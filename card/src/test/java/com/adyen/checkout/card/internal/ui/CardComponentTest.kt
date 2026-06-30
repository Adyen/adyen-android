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
import com.adyen.checkout.core.analytics.internal.TestAnalyticsManager
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.internal.data.provider.SdkDataProvider
import com.adyen.checkout.cse.internal.BaseCardEncryptor
import com.adyen.checkout.cse.internal.BaseGenericEncryptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class)
internal class CardComponentTest(
    @Mock private val cardEncryptor: BaseCardEncryptor,
    @Mock private val genericEncryptor: BaseGenericEncryptor,
    @Mock private val detectCardTypeRepository: DetectCardTypeRepository,
    @Mock private val sdkDataProvider: SdkDataProvider,
    @Mock private val cardScannerWrapper: CardScannerWrapper,
) {

    private lateinit var analyticsManager: TestAnalyticsManager
    private lateinit var cardComponentParams: CardComponentParams
    private lateinit var coroutineScope: CoroutineScope
    private lateinit var component: CardComponent

    @BeforeEach
    fun beforeEach() {
        analyticsManager = TestAnalyticsManager()
        cardComponentParams = createCardComponentParams()
        coroutineScope = CoroutineScope(UnconfinedTestDispatcher())
        component = createCardComponent()
    }

    @AfterEach
    fun afterEach() {
        coroutineScope.cancel()
    }

    @Nested
    @DisplayName("when brand state transitions to DualBrandWithShopperSelection")
    inner class SubscribeToDualBrandAnalyticsEventsTest {

        @Test
        fun `then dualBrandSelectionDisplayed event is tracked`() {
            // WHEN
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createDualBrandSelectableList()))

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
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createDualBrandSelectableList()))
            component.handleIntent(CardIntent.UpdateDetectedCardTypes(createDualBrandSelectableList()))

            // THEN - distinctUntilChanged prevents duplicate events for repeated DualBrandWithShopperSelection state
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
    }

    @Nested
    @DisplayName("when intent is SelectBrand")
    inner class SelectBrandTest {

        @Test
        fun `and brand is different from currently selected then brandSelected event is tracked`() {
            // GIVEN
            setupDualBrandWithShopperSelectionState()

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
            setupDualBrandWithShopperSelectionState()

            // WHEN - current selection is "visa", re-selecting "visa"
            component.handleIntent(CardIntent.SelectBrand(CardBrand("visa")))

            // THEN - last event is the dualBrandSelectionDisplayed event, not a brandSelected event
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

    private fun setupDualBrandWithShopperSelectionState() {
        component.handleIntent(CardIntent.UpdateDetectedCardTypes(createDualBrandSelectableList()))
    }

    private fun createDualBrandSelectableList() = DetectedCardTypeList(
        detectedCardTypes = listOf(
            createDetectedCardType().copy(cardBrand = CardBrand("visa")),
            createDetectedCardType().copy(
                cardBrand = CardBrand("cartebancaire"),
                isShopperSelectionAllowedInDualBranded = true,
            ),
        ),
        source = DetectedCardTypeList.Source.NETWORK,
        cardDetectionBin = null,
        issuingCountryCode = null,
    )

    private fun createCardComponent(): CardComponent {
        val detectCardTypeBinHelper = DetectCardTypeBinHelper()
        val cardBrandIntentsHandler = CardBrandIntentsHandler(cardComponentParams, detectCardTypeBinHelper)
        val reducer = CardComponentStateReducer(cardBrandIntentsHandler)
        val factory = CardComponentStateFactory(cardComponentParams)
        val validator = CardComponentStateValidator(CardValidationMapper())
        return CardComponent(
            analyticsManager = analyticsManager,
            cardEncryptor = cardEncryptor,
            genericEncryptor = genericEncryptor,
            componentParams = cardComponentParams,
            detectCardTypeRepository = detectCardTypeRepository,
            componentStateValidator = validator,
            componentStateFactory = factory,
            componentStateReducer = reducer,
            viewStateProducer = CardViewStateProducer(),
            coroutineScope = coroutineScope,
            sdkDataProvider = sdkDataProvider,
            paymentMethodType = PAYMENT_METHOD_TYPE,
            onBinChangeCallback = null,
            onBinLookupCallback = null,
            cardScannerWrapper = cardScannerWrapper,
            publicKey = null,
            environment = Environment.TEST,
        )
    }

    private fun createCardComponentParams() = CardComponentParams(
        showCardholderName = false,
        supportedCardBrands = emptyList(),
        showStorePaymentMethod = false,
        showSupportedCardBrandLogos = true,
        socialSecurityNumberVisibility = FieldVisibility.HIDE,
        koreanAuthenticationVisibility = FieldVisibility.HIDE,
        showPostalCode = false,
        cvcVisibility = CVCVisibility.ALWAYS_SHOW,
        storedCVCVisibility = StoredCVCVisibility.SHOW,
        showCardScanner = false,
        installmentParams = null,
    )

    private fun createDetectedCardType() = DetectedCardType(
        cardBrand = CardBrand("visa"),
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        isHidden = false,
        isShopperSelectionAllowedInDualBranded = false,
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

    companion object {
        private const val PAYMENT_METHOD_TYPE = "scheme"
    }
}
