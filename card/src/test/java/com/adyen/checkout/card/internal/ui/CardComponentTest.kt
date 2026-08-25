/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by temirlan on 29/6/2026.
 */

package com.adyen.checkout.card.internal.ui

import com.adyen.checkout.card.FieldVisibility
import com.adyen.checkout.card.internal.analytics.DualBrandCardEvents
import com.adyen.checkout.card.internal.data.api.DetectCardTypeRepository
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.data.model.DetectedCardTypeList
import com.adyen.checkout.card.internal.helper.CardConfigDataGenerator
import com.adyen.checkout.card.internal.helper.DetectCardTypeBinHelper
import com.adyen.checkout.card.internal.ui.model.CVCVisibility
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.StoredCVCVisibility
import com.adyen.checkout.card.internal.ui.state.CardBrandData
import com.adyen.checkout.card.internal.ui.state.CardBrandIntentsHandler
import com.adyen.checkout.card.internal.ui.state.CardComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardComponentStateReducer
import com.adyen.checkout.card.internal.ui.state.CardComponentStateValidator
import com.adyen.checkout.card.internal.ui.state.CardIntent
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentState
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.util.CardScannerWrapper
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.core.components.paymentmethod.CardDetails
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class CardComponentTest(
    @param:Mock private val cardEncryptor: BaseCardEncryptor,
    @param:Mock private val genericEncryptor: BaseGenericEncryptor,
    @param:Mock private val detectCardTypeRepository: DetectCardTypeRepository,
    @param:Mock private val sdkDataProvider: SdkDataProvider,
    @param:Mock private val cardScannerWrapper: CardScannerWrapper,
    @param:Mock private val cardConfigDataGenerator: CardConfigDataGenerator,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var component: CardComponent

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        component = createComponent()
    }

    @Test
    fun `when component is initialized then rendered event is tracked`() {
        // GIVEN
        val configData = mapOf("testKey" to "testValue")
        whenever(cardConfigDataGenerator.generate(params = any(), isStored = eq(false))).thenReturn(configData)
        analyticsManager = TestAnalyticsManager()

        // WHEN
        createComponent()

        // THEN
        val expected = GenericEvents.rendered(
            component = PAYMENT_METHOD_TYPE,
            configData = configData,
        )
        analyticsManager.assertHasEventEquals(expected)
    }

    @Nested
    @DisplayName("when brand state transitions to DualBrandWithShopperSelection")
    inner class SubscribeToDualBrandSelectionAppearAnalyticsEventsTest {

        @Test
        fun `then dualBrandSelectionDisplayed event is tracked`() {
            // WHEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // THEN
            val expected = DualBrandCardEvents.dualBrandSelectionDisplayed(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
                brandOptions = listOf(
                    createCardBrandData(CardBrand("visa")),
                    createCardBrandData(CardBrand("cartebancaire")),
                ),
            )
            analyticsManager.assertHasEventEquals(expected)
        }

        @Test
        fun `and same state is emitted again then event is tracked only once`() {
            // WHEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // THEN
            val expected = DualBrandCardEvents.dualBrandSelectionDisplayed(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
                brandOptions = listOf(
                    createCardBrandData(CardBrand("visa")),
                    createCardBrandData(CardBrand("cartebancaire")),
                ),
            )
            analyticsManager.assertEventCount(1, expected)
        }

        @Test
        fun `and shopper changes brand selection within DualBrandWithShopperSelection then no new event is tracked`() {
            // GIVEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // WHEN - change selection from auto-selected "visa" to "cartebancaire"
            component.handleIntent(CardIntent.SelectBrand(CardBrand("cartebancaire")))

            // THEN - event is tracked only once (on appear, not on brand selection change)
            val expected = DualBrandCardEvents.dualBrandSelectionDisplayed(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
                brandOptions = listOf(
                    createCardBrandData(CardBrand("visa")),
                    createCardBrandData(CardBrand("cartebancaire")),
                ),
            )
            analyticsManager.assertEventCount(1, expected)
        }

        @Test
        fun `and brand state transitions to non-dual-brand then no new event is tracked`() {
            // GIVEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // WHEN - clear detected brands (simulates card number being cleared)
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createEmptyDetectedCardTypeList()))

            // THEN - event is tracked only once (on appear, not on disappear)
            val expected = DualBrandCardEvents.dualBrandSelectionDisplayed(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
                brandOptions = listOf(
                    createCardBrandData(CardBrand("visa")),
                    createCardBrandData(CardBrand("cartebancaire")),
                ),
            )
            analyticsManager.assertEventCount(1, expected)
        }

        @Test
        fun `and dual brand state disappears and reappears then event is tracked again`() {
            // WHEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createEmptyDetectedCardTypeList()))
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // THEN
            val expected = DualBrandCardEvents.dualBrandSelectionDisplayed(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
                brandOptions = listOf(
                    createCardBrandData(CardBrand("visa")),
                    createCardBrandData(CardBrand("cartebancaire")),
                ),
            )
            analyticsManager.assertEventCount(2, expected)
        }
    }

    @Nested
    @DisplayName("when intent is SelectBrand")
    inner class SelectBrandTest {

        @Test
        fun `and brand is different from currently selected then brandSelected event is tracked`() {
            // GIVEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // WHEN - current selection is "visa" (first brand), selecting "cartebancaire"
            component.handleIntent(CardIntent.SelectBrand(CardBrand("cartebancaire")))

            // THEN
            val expected = DualBrandCardEvents.brandSelected(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("cartebancaire"),
            )
            analyticsManager.assertLastEventEquals(expected)
        }

        @Test
        fun `and brand is same as currently selected then no brandSelected event is tracked`() {
            // GIVEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createSelectableDetectedCardBrandList()))

            // WHEN - current selection is "visa", re-selecting "visa"
            component.handleIntent(CardIntent.SelectBrand(CardBrand("visa")))

            // THEN
            val notExpected = DualBrandCardEvents.brandSelected(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
            )
            analyticsManager.assertLastEventNotEquals(notExpected)
        }

        @Test
        fun `and brand state is not DualBrandWithShopperSelection then no brandSelected event is tracked`() {
            // GIVEN - initial state is NoBrandsDetected, no setup

            // WHEN
            component.handleIntent(CardIntent.SelectBrand(CardBrand("visa")))

            // THEN - no events tracked at all
            val notExpected = DualBrandCardEvents.brandSelected(
                component = PAYMENT_METHOD_TYPE,
                selectedBrand = CardBrand("visa"),
            )
            analyticsManager.assertLastEventNotEquals(notExpected)
        }
    }

    @Nested
    @DisplayName("when submit is called")
    inner class SubmitTest {

        @Test
        fun `and state is valid then Submit event is emitted`() = runTest {
            // GIVEN
            mockSuccessfulEncryption()
            val component = createComponent(publicKey = TEST_PUBLIC_KEY)
            fillInValidCard(component)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            val event = eventFlow.latestValue
            assertInstanceOf(PaymentComponentEvent.Submit::class.java, event)
            assertTrue((event as PaymentComponentEvent.Submit).state.isValid)
        }

        @Test
        fun `and funding source is set then it is part of the card details`() = runTest {
            // GIVEN
            mockSuccessfulEncryption()
            val component = createComponent(publicKey = TEST_PUBLIC_KEY, fundingSource = "debit")
            fillInValidCard(component)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            val state = (eventFlow.latestValue as PaymentComponentEvent.Submit).state
            assertInstanceOf(CardPaymentComponentState::class.java, state)
            assertEquals(createExpectedCardDetails(fundingSource = "debit"), state.data.paymentMethod)
        }

        @Test
        fun `and funding source is null then card details funding source is null`() = runTest {
            // GIVEN
            mockSuccessfulEncryption()
            val component = createComponent(publicKey = TEST_PUBLIC_KEY, fundingSource = null)
            fillInValidCard(component)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            val state = (eventFlow.latestValue as PaymentComponentEvent.Submit).state
            assertEquals(createExpectedCardDetails(fundingSource = null), state.data.paymentMethod)
        }

        private fun mockSuccessfulEncryption() {
            whenever(cardEncryptor.encryptFields(any(), any())).thenReturn(
                EncryptedCard(
                    encryptedCardNumber = "encrypted_card_number",
                    encryptedExpiryMonth = "encrypted_expiry_month",
                    encryptedExpiryYear = "encrypted_expiry_year",
                    encryptedSecurityCode = null,
                ),
            )
            whenever(sdkDataProvider.createEncodedSdkData(any())).thenReturn("sdk_data")
        }

        private fun fillInValidCard(component: CardComponent) {
            whenever(detectCardTypeRepository.detectCardTypes(any())).thenReturn(emptyFlow())
            component.handleIntent(CardIntent.UpdateCardNumber("4111111111111111"))
            component.handleIntent(CardIntent.UpdateExpiryDate("1230"))
        }

        private fun createExpectedCardDetails(fundingSource: String?) = CardDetails(
            type = PAYMENT_METHOD_TYPE,
            sdkData = "sdk_data",
            encryptedCardNumber = "encrypted_card_number",
            encryptedExpiryMonth = "encrypted_expiry_month",
            encryptedExpiryYear = "encrypted_expiry_year",
            encryptedSecurityCode = null,
            encryptedPassword = null,
            holderName = null,
            taxNumber = null,
            brand = null,
            fundingSource = fundingSource,
        )
    }

    private fun createEmptyDetectedCardTypeList() = DetectedCardTypeList(
        detectedCardTypes = emptyList(),
        source = DetectedCardTypeList.Source.NETWORK,
        cardDetectionBin = null,
        issuingCountryCode = null,
    )

    private fun createSelectableDetectedCardBrandList() = DetectedCardTypeList(
        detectedCardTypes = listOf(
            createDetectedSelectableCardType().copy(
                cardBrand = CardBrand("visa"),
            ),
            createDetectedSelectableCardType().copy(
                cardBrand = CardBrand("cartebancaire"),
            ),
        ),
        source = DetectedCardTypeList.Source.NETWORK,
        cardDetectionBin = null,
        issuingCountryCode = null,
    )

    private fun createDetectedSelectableCardType() = DetectedCardType(
        cardBrand = CardBrand(""),
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        isHidden = false,
        isShopperSelectionAllowedInDualBranded = true,
        panLength = null,
        paymentMethodVariant = null,
        localizedBrand = null,
    )

    private fun createCardBrandData(cardBrand: CardBrand) = CardBrandData(
        cardBrand = cardBrand,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        panLength = null,
        paymentMethodVariant = null,
        localizedBrand = null,
    )

    private fun createComponent(
        cardComponentParams: CardComponentParams = createCardComponentParams(),
        publicKey: String? = null,
        fundingSource: String? = null,
    ): CardComponent {
        val cardBrandIntentsHandler = CardBrandIntentsHandler(
            componentParams = cardComponentParams,
            detectCardTypeBinHelper = DetectCardTypeBinHelper(),
        )
        return CardComponent(
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            genericEncryptor = genericEncryptor,
            componentParams = cardComponentParams,
            detectCardTypeRepository = detectCardTypeRepository,
            componentStateValidator = CardComponentStateValidator(CardValidationMapper()),
            componentStateFactory = CardComponentStateFactory(cardComponentParams),
            componentStateReducer = CardComponentStateReducer(cardBrandIntentsHandler),
            viewStateProducer = CardViewStateProducer(amount = null, showSubmitButton = true),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            sdkDataProvider = sdkDataProvider,
            paymentMethodType = PAYMENT_METHOD_TYPE,
            onBinChangeCallback = null,
            onBinLookupCallback = null,
            cardScannerWrapper = cardScannerWrapper,
            publicKey = publicKey,
            environment = Environment.TEST,
            fundingSource = fundingSource,
            cardConfigDataGenerator = cardConfigDataGenerator,
        )
    }

    private fun createCardComponentParams() = CardComponentParams(
        showCardholderName = false,
        supportedCardBrands = emptyList(),
        showStorePaymentMethod = false,
        showSupportedCardBrandLogos = false,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility = FieldVisibility.HIDE,
        showPostalCode = false,
        cvcVisibility = CVCVisibility.ALWAYS_HIDE,
        storedCVCVisibility = StoredCVCVisibility.HIDE,
        showCardScanner = false,
        installmentParams = null,
    )

    companion object {
        private const val PAYMENT_METHOD_TYPE = "scheme"
        private const val TEST_PUBLIC_KEY = "test_public_key"
    }
}
