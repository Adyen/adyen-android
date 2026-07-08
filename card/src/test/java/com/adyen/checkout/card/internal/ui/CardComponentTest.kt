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
import com.adyen.checkout.card.internal.ui.state.CardValidationMapper
import com.adyen.checkout.card.internal.ui.state.CardViewStateProducer
import com.adyen.checkout.card.internal.util.CardScannerWrapper
import com.adyen.checkout.core.analytics.internal.GenericEvents
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
        cardComponentParams: CardComponentParams = createCardComponentParams()
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
            viewStateProducer = CardViewStateProducer(),
            coroutineScope = CoroutineScope(UnconfinedTestDispatcher()),
            sdkDataProvider = sdkDataProvider,
            paymentMethodType = PAYMENT_METHOD_TYPE,
            onBinChangeCallback = null,
            onBinLookupCallback = null,
            cardScannerWrapper = cardScannerWrapper,
            publicKey = null,
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
    }
}
