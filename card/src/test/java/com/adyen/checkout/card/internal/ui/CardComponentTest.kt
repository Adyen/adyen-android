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
import com.adyen.checkout.card.internal.ui.state.CardPaymentComponentStateFactory
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.util.CardScannerWrapper
import com.adyen.checkout.core.analytics.internal.ErrorEvent
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.cse.EncryptedCard
import com.adyen.checkout.cse.internal.TestCardEncryptor
import com.adyen.checkout.cse.internal.TestGenericEncryptor
import com.adyen.checkout.test.extensions.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertInstanceOf
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class CardComponentTest(
    @param:Mock private val detectCardTypeRepository: DetectCardTypeRepository,
    @param:Mock private val cardPaymentComponentStateFactory: CardPaymentComponentStateFactory,
    @param:Mock private val cardScannerWrapper: CardScannerWrapper,
    @param:Mock private val cardConfigDataGenerator: CardConfigDataGenerator,
) {

    private lateinit var cardEncryptor: TestCardEncryptor
    private lateinit var genericEncryptor: TestGenericEncryptor
    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var component: CardComponent

    @BeforeEach
    fun beforeEach() {
        cardEncryptor = TestCardEncryptor()
        genericEncryptor = TestGenericEncryptor()
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
        fun `and state is invalid then no event is emitted`() = runTest {
            // GIVEN - the card number and the expiry date are empty
            val component = createComponent(publicKey = TEST_PUBLIC_KEY)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertTrue(eventFlow.values.isEmpty())
        }

        @Test
        fun `and state is invalid then the validation errors are highlighted`() = runTest {
            // GIVEN - the card number and the expiry date are empty
            val componentStateReducer = spy(createComponentStateReducer(createCardComponentParams()))
            val component = createComponent(
                publicKey = TEST_PUBLIC_KEY,
                componentStateReducer = componentStateReducer,
            )

            // WHEN
            component.submit()

            // THEN
            verify(componentStateReducer).reduce(any(), eq(CardIntent.HighlightValidationErrors))
        }

        @Test
        fun `and state is valid then the state created by the factory is submitted`() = runTest {
            // GIVEN
            val expectedState = mockCreatedPaymentComponentState()
            val component = createValidComponent()
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            val event = eventFlow.latestValue
            assertInstanceOf<PaymentComponentEvent.Submit>(event)
            assertEquals(expectedState, event.state)
        }

        @Test
        fun `and state is valid then the card number, expiry date and security code are encrypted`() = runTest {
            // GIVEN
            mockCreatedPaymentComponentState()
            val component = createValidComponent(
                cardComponentParams = createCardComponentParams().copy(cvcVisibility = CVCVisibility.ALWAYS_SHOW),
            )
            component.handleIntent(CardIntent.UpdateSecurityCode("737"))

            // WHEN
            component.submit()

            // THEN
            verify(cardPaymentComponentStateFactory).createPaymentComponentState(
                cardComponentState = any(),
                encryptedCard = eq(createExpectedEncryptedCard(securityCode = "737")),
                encryptedKcpCardPassword = anyOrNull(),
            )
        }

        @Test
        fun `and the security code is hidden then it is not encrypted`() = runTest {
            // GIVEN - a security code was filled in while it was still visible
            mockCreatedPaymentComponentState()
            val component = createValidComponent(
                cardComponentParams = createCardComponentParams().copy(cvcVisibility = CVCVisibility.ALWAYS_HIDE),
            )
            component.handleIntent(CardIntent.UpdateSecurityCode("737"))

            // WHEN
            component.submit()

            // THEN
            verify(cardPaymentComponentStateFactory).createPaymentComponentState(
                cardComponentState = any(),
                encryptedCard = eq(createExpectedEncryptedCard(securityCode = null)),
                encryptedKcpCardPassword = anyOrNull(),
            )
        }

        @Test
        fun `and publicKey is null then an Error event is emitted and nothing is submitted`() = runTest {
            // GIVEN
            val component = createValidComponent(publicKey = null)
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertInstanceOf<PaymentComponentEvent.Error>(eventFlow.latestValue)
            assertTrue(eventFlow.values.none { it is PaymentComponentEvent.Submit })
        }

        @Test
        fun `and publicKey is null then API_PUBLIC_KEY analytics error is tracked`() = runTest {
            // GIVEN
            val component = createValidComponent(publicKey = null)

            // WHEN
            component.submit()

            // THEN
            val expected = GenericEvents.error(PAYMENT_METHOD_TYPE, ErrorEvent.API_PUBLIC_KEY)
            analyticsManager.assertHasEventEquals(expected)
        }

        @Test
        fun `and card encryption fails then an Error event is emitted and nothing is submitted`() = runTest {
            // GIVEN
            cardEncryptor.shouldThrowException = true
            val component = createValidComponent()
            val eventFlow = component.eventFlow.test(testScheduler)

            // WHEN
            component.submit()

            // THEN
            assertInstanceOf<PaymentComponentEvent.Error>(eventFlow.latestValue)
            assertTrue(eventFlow.values.none { it is PaymentComponentEvent.Submit })
        }

        @Test
        fun `and card encryption fails then ENCRYPTION analytics error is tracked`() = runTest {
            // GIVEN
            cardEncryptor.shouldThrowException = true
            val component = createValidComponent()

            // WHEN
            component.submit()

            // THEN
            val expected = GenericEvents.error(PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
            analyticsManager.assertHasEventEquals(expected)
        }

        @Test
        fun `and the kcp card password is filled in then it is encrypted and passed to the factory`() = runTest {
            // GIVEN - TestGenericEncryptor echoes the value it encrypts
            mockCreatedPaymentComponentState()
            val component = createKoreanAuthenticationComponent()

            // WHEN
            component.submit()

            // THEN
            verify(cardPaymentComponentStateFactory).createPaymentComponentState(
                cardComponentState = any(),
                encryptedCard = any(),
                encryptedKcpCardPassword = eq("12"),
            )
        }

        @Test
        fun `and the kcp card password is not filled in then no password is passed to the factory`() = runTest {
            // GIVEN
            mockCreatedPaymentComponentState()
            val component = createValidComponent()

            // WHEN
            component.submit()

            // THEN
            verify(cardPaymentComponentStateFactory).createPaymentComponentState(
                cardComponentState = any(),
                encryptedCard = any(),
                encryptedKcpCardPassword = eq(null),
            )
        }

        @Test
        fun `and the kcp password encryption fails then an Error event is emitted and nothing is submitted`() =
            runTest {
                // GIVEN
                genericEncryptor.shouldThrowException = true
                val component = createKoreanAuthenticationComponent()
                val eventFlow = component.eventFlow.test(testScheduler)

                // WHEN
                component.submit()

                // THEN
                assertInstanceOf<PaymentComponentEvent.Error>(eventFlow.latestValue)
                assertTrue(eventFlow.values.none { it is PaymentComponentEvent.Submit })
            }

        @Test
        fun `and the kcp card password encryption fails then ENCRYPTION analytics error is tracked`() = runTest {
            // GIVEN
            genericEncryptor.shouldThrowException = true
            val component = createKoreanAuthenticationComponent()

            // WHEN
            component.submit()

            // THEN
            val expected = GenericEvents.error(PAYMENT_METHOD_TYPE, ErrorEvent.ENCRYPTION)
            analyticsManager.assertHasEventEquals(expected)
        }

        /**
         * [TestCardEncryptor] echoes the values it encrypts, so the expected result mirrors the filled in card.
         */
        private fun createExpectedEncryptedCard(securityCode: String?) = EncryptedCard(
            encryptedCardNumber = TEST_CARD_NUMBER,
            encryptedExpiryMonth = "12",
            encryptedExpiryYear = "2030",
            encryptedSecurityCode = securityCode,
        )

        private fun mockCreatedPaymentComponentState(): CardPaymentComponentState {
            val state = CardPaymentComponentState(
                data = PaymentComponentData(paymentMethod = null, order = null),
                isValid = true,
            )
            whenever(
                cardPaymentComponentStateFactory.createPaymentComponentState(any(), any(), anyOrNull()),
            ).thenReturn(state)
            return state
        }

        /**
         * Creates a component with a valid card number and expiry date filled in, so that it can be submitted.
         */
        private fun createValidComponent(
            cardComponentParams: CardComponentParams = createCardComponentParams(),
            publicKey: String? = TEST_PUBLIC_KEY,
        ): CardComponent {
            val component = createComponent(
                cardComponentParams = cardComponentParams,
                publicKey = publicKey,
            )
            whenever(detectCardTypeRepository.detectCardTypes(any())).thenReturn(emptyFlow())
            component.handleIntent(CardIntent.UpdateCardNumber(TEST_CARD_NUMBER))
            component.handleIntent(CardIntent.UpdateExpiryDate("1230"))
            return component
        }

        private fun createKoreanAuthenticationComponent(): CardComponent {
            val component = createValidComponent(
                cardComponentParams = createCardComponentParams()
                    .copy(koreanAuthenticationVisibility = FieldVisibility.SHOW),
            )
            component.handleIntent(CardIntent.UpdateKcpBirthDateOrTaxNumber("260403"))
            component.handleIntent(CardIntent.UpdateKcpCardPassword("12"))
            return component
        }
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

    private fun createComponentStateReducer(cardComponentParams: CardComponentParams) = CardComponentStateReducer(
        CardBrandIntentsHandler(
            componentParams = cardComponentParams,
            detectCardTypeBinHelper = DetectCardTypeBinHelper(),
        ),
    )

    private fun createComponent(
        cardComponentParams: CardComponentParams = createCardComponentParams(),
        publicKey: String? = null,
        componentStateReducer: CardComponentStateReducer = createComponentStateReducer(cardComponentParams),
    ): CardComponent {
        return CardComponent(
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            genericEncryptor = genericEncryptor,
            componentParams = cardComponentParams,
            detectCardTypeRepository = detectCardTypeRepository,
            componentStateValidator = CardComponentStateValidator(CardValidationMapper()),
            componentStateFactory = CardComponentStateFactory(cardComponentParams),
            componentStateReducer = componentStateReducer,
            viewStateProducer = CardViewStateProducer(amount = null, showSubmitButton = true),
            cardPaymentComponentStateFactory = cardPaymentComponentStateFactory,
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            paymentMethodType = PAYMENT_METHOD_TYPE,
            onBinChangeCallback = null,
            onBinLookupCallback = null,
            cardScannerWrapper = cardScannerWrapper,
            publicKey = publicKey,
            environment = Environment.TEST,
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
        private const val TEST_CARD_NUMBER = "4111111111111111"
    }
}
