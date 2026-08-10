/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by andriim on 22/1/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TrailingIcon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

internal class CardViewStateProducerTest {

    private lateinit var producer: CardViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = CardViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = true)
    }

    @Test
    fun `when produce is called, then pay button state is propagated to the view state`() {
        // GIVEN
        val componentState = createComponentState()

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(PayButtonViewState(TEST_AMOUNT, false), viewState.payButtonViewState)
    }

    @Test
    fun `when show submit button is false then pay button view state is null`() {
        // GIVEN
        val producer = CardViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = false)
        val componentState = createComponentState()

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertNull(viewState.payButtonViewState)
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when no card brand is detected, then supported card brands should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.NoBrandsDetected,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.supportedCardBrandsViewState.isVisible)
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when supported card brand is detected, then supported card brands should be hidden`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when unsupported card brand is detected, then supported card brands should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.UnsupportedBrand,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.supportedCardBrandsViewState.isVisible)
    }

    @Test
    fun `when hidden brand is detected, then supported card brands should be shown and card brand view state should be placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.HiddenBrand,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.supportedCardBrandsViewState.isVisible)
        assertEquals(CardBrandViewState.Placeholder, viewState.cardBrandViewState)
    }

    @Test
    fun `when single reliable with hidden brand is detected, then supported card brands should be hidden and card brand view state should be single brand`() {
        // GIVEN
        val cardBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.SingleReliableWithHiddenBrand(cardBrandData),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
        assertEquals(CardBrandViewState.SingleBrand(CardBrand("visa")), viewState.cardBrandViewState)
    }

    @Test
    fun `when dual brand is detected, then card brand view state should be dual brand`() {
        // GIVEN
        val visaBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val mcBrandData = getCardBrandData().copy(cardBrand = CardBrand("mc"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.DualBrand(listOf(visaBrandData, mcBrandData)),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
        assertEquals(
            CardBrandViewState.DualBrand(listOf(CardBrand("visa"), CardBrand("mc"))),
            viewState.cardBrandViewState,
        )
    }

    @Test
    fun `when dual brand with shopper selection is detected, then card brand view state should be selectable dual brand`() {
        // GIVEN
        val visaBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val mcBrandData = getCardBrandData().copy(cardBrand = CardBrand("mc"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(visaBrandData, mcBrandData),
                shopperSelectedCardBrandData = visaBrandData,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
        assertEquals(
            CardBrandViewState.SelectableDualBrand(
                listOf(
                    SelectableCardBrandItem(brand = CardBrand("visa"), isSelected = true),
                    SelectableCardBrandItem(brand = CardBrand("mc"), isSelected = false),
                ),
            ),
            viewState.cardBrandViewState,
        )
    }

    @Test
    fun `when amex brand is detected, then cardNumberFormat should be Amex`() {
        // GIVEN
        val amexBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("amex"),
        )
        val componentState = createComponentState(
            cardBrandState = CardBrandState.SingleReliableBrand(amexBrandData),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CardNumberFormat.AMEX, viewState.cardNumberFormat)
    }

    @Test
    fun `when non-amex brand is detected, then cardNumberFormat should be Default`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.SingleReliableBrand(
                getCardBrandData().copy(cardBrand = CardBrand("visa")),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, viewState.cardNumberFormat)
    }

    @Test
    fun `when no brand is detected, then cardNumberFormat should be Default`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.NoBrandsDetected,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, viewState.cardNumberFormat)
    }

    @Test
    fun `when dual brand with amex selected, then cardNumberFormat should be Amex`() {
        // GIVEN
        val amexBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("amex"),
        )
        val visaBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(amexBrandData, visaBrandData),
                shopperSelectedCardBrandData = amexBrandData,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CardNumberFormat.AMEX, viewState.cardNumberFormat)
    }

    @Test
    fun `when dual brand with non-amex selected, then cardNumberFormat should be Default`() {
        // GIVEN
        val amexBrandData = getCardBrandData().copy(
            cardBrand = CardBrand("amex"),
        )
        val visaBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(amexBrandData, visaBrandData),
                shopperSelectedCardBrandData = visaBrandData,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CardNumberFormat.DEFAULT, viewState.cardNumberFormat)
    }

    @Test
    fun `when dual brand with shopper selection is detected, then card number supportingText is dual brand selector description`() {
        // GIVEN
        val visaBrandData = getCardBrandData().copy(cardBrand = CardBrand("visa"))
        val mcBrandData = getCardBrandData().copy(cardBrand = CardBrand("mc"))
        val componentState = createComponentState(
            cardBrandState = CardBrandState.DualBrandWithShopperSelection(
                cardBrandDataList = listOf(visaBrandData, mcBrandData),
                shopperSelectedCardBrandData = visaBrandData,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(CheckoutLocalizationKey.CARD_DUAL_BRAND_SELECTOR_DESCRIPTION, viewState.cardNumber?.supportingText)
    }

    @Test
    fun `when no brand is detected, then card number supportingText is null`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.NoBrandsDetected,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertNull(viewState.cardNumber?.supportingText)
    }

    // UC6: Error Hides Brand Logos
    @Test
    fun `when card number has error with detected brand, then trailing icon is error and supported brands are hidden`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                showError = true,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(
            TrailingIcon.Error,
            viewState.cardNumber?.trailingIcon,
        )
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
    }

    // UC7: Re-entering Field Clears Error - brand detected scenario
    @Test
    fun `when field with error gains focus and brand is detected, then error is cleared and logos stay hidden`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                isFocused = true,
                showError = false, // Focus gain clears showError
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(false, viewState.cardNumber?.isError)
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
    }

    // UC7: Re-entering Field Clears Error - no brand detected scenario
    @Test
    fun `when field with error gains focus and no brand is detected, then error is cleared and logos reappear`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "123",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                isFocused = true,
                showError = false, // Focus gain clears showError
            ),
            cardBrandState = CardBrandState.NoBrandsDetected,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(false, viewState.cardNumber?.isError)
        assertTrue(viewState.supportedCardBrandsViewState.isVisible)
    }

    // UC8: Valid Input Hides Logos
    @Test
    fun `when card number is valid with detected brand, then supported card brands are hidden and trailing icon is brand logos`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111111111111111",
                errorMessage = null,
                showError = false,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
        assertEquals(false, viewState.cardNumber?.isError)
        assertEquals(
            CardNumberTrailingIcon.BrandLogos,
            viewState.cardNumber?.trailingIcon,
        )
    }

    // UC9: Partial Input While Typing
    @Test
    fun `when user is typing with partial input and brand is detected, then view is updated correctly`() {
        // GIVEN
        val componentStateWithHiddenError = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                isFocused = true,
                showError = false, // Text change sets showError to false
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )
        val componentStateWithoutError = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                isFocused = true,
                showError = false,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData()),
        )

        listOf(componentStateWithHiddenError, componentStateWithoutError).forEach { componentState ->
            // WHEN
            val viewState = producer.produce(componentState)

            // THEN
            assertEquals(false, viewState.cardNumber?.isError)
            assertFalse(viewState.supportedCardBrandsViewState.isVisible)
            assertEquals(
                CardNumberTrailingIcon.BrandLogos,
                viewState.cardNumber?.trailingIcon,
            )
        }
    }

    @Test
    fun `when supported card brands contain hidden brands, then hidden brands are excluded from view state`() {
        // GIVEN
        val componentState = createComponentState(
            supportedCardBrands = listOf(
                CardBrand("visa"),
                CardBrand("accel"),
                CardBrand("mc"),
                CardBrand("pulse"),
                CardBrand("star"),
                CardBrand("nyce"),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(
            listOf(CardBrand("visa"), CardBrand("mc")),
            viewState.supportedCardBrandsViewState.supportedCardBrands,
        )
    }

    @Test
    fun `when showSupportedCardBrandLogos is false and no brand is detected, then supported card brands are hidden`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.NoBrandsDetected,
            showSupportedCardBrandLogos = false,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
    }

    @Test
    fun `when showSupportedCardBrandLogos is false and unsupported brand is detected, then supported card brands are hidden`() {
        // GIVEN
        val componentState = createComponentState(
            cardBrandState = CardBrandState.UnsupportedBrand,
            showSupportedCardBrandLogos = false,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.supportedCardBrandsViewState.isVisible)
    }

    @Test
    fun `when card scanning is available and card number is empty, then scan button is visible and trailing icon is ScanButton`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(text = ""),
            isCardScanningAvailable = true,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.isCardScanButtonVisible)
        assertEquals(CardNumberTrailingIcon.ScanButton, viewState.cardNumber?.trailingIcon)
    }

    @Test
    fun `when card scanning is available and card number is not empty, then scan button is not visible`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(text = "4111"),
            isCardScanningAvailable = true,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.isCardScanButtonVisible)
    }

    @Test
    fun `when card scanning is available and card number is showing an error, then trailing icon is Error`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                showError = true,
            ),
            isCardScanningAvailable = true,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.isCardScanButtonVisible)
        assertEquals(TrailingIcon.Error, viewState.cardNumber?.trailingIcon)
    }

    @Test
    fun `when expiry date is valid, then trailing icon is Checkmark`() {
        // GIVEN
        val componentState = createComponentState(
            expiryDate = TextInputComponentState(text = "0330"),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(ExpiryDateTrailingIcon.Checkmark, viewState.expiryDate?.trailingIcon)
    }

    @Test
    fun `when expiry date is empty, then trailing icon is Placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            expiryDate = TextInputComponentState(text = ""),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(ExpiryDateTrailingIcon.Placeholder, viewState.expiryDate?.trailingIcon)
    }

    @Test
    fun `when expiry date is partially filled, then trailing icon is Placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            expiryDate = TextInputComponentState(
                text = "12",
                errorMessage = CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(ExpiryDateTrailingIcon.Placeholder, viewState.expiryDate?.trailingIcon)
    }

    @Test
    fun `when expiry date is optional and empty, then trailing icon is Placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            expiryDate = TextInputComponentState(
                text = "",
                requirementPolicy = RequirementPolicy.Optional,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(ExpiryDateTrailingIcon.Placeholder, viewState.expiryDate?.trailingIcon)
    }

    @Test
    fun `when expiry date is showing an error, then trailing icon is Error instead of Placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            expiryDate = TextInputComponentState(
                text = "6415",
                errorMessage = CheckoutLocalizationKey.CARD_EXPIRY_DATE_INVALID,
                showError = true,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(TrailingIcon.Error, viewState.expiryDate?.trailingIcon)
    }

    @Test
    fun `when security code is valid, then trailing icon is Checkmark`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(text = "123"),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.Checkmark, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is empty and amex is detected, then trailing icon is PlaceholderAmex`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(text = ""),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData(CardBrand("amex"))),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderAmex, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is empty and non-amex is detected, then trailing icon is PlaceholderDefault`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(text = ""),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData(CardBrand("visa"))),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderDefault, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is partially filled and non-amex is detected, then trailing icon is PlaceholderDefault`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "12",
                errorMessage = CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData(CardBrand("visa"))),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderDefault, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is optional and empty, then trailing icon is PlaceholderDefault`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "",
                requirementPolicy = RequirementPolicy.Optional,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData(CardBrand("visa"))),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderDefault, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is showing an error, then trailing icon is Error instead of PlaceholderDefault`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "12",
                errorMessage = CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID,
                showError = true,
            ),
            cardBrandState = CardBrandState.SingleReliableBrand(getCardBrandData(CardBrand("visa"))),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(TrailingIcon.Error, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when card scanning is not available and card number is empty, then scan button is not visible`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(text = ""),
            isCardScanningAvailable = false,
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.isCardScanButtonVisible)
    }

    @Test
    fun `when installment state has options and selection, then viewState contains same options and selection`() {
        // GIVEN
        val options = listOf(
            InstallmentModel.Regular(3, amountPerInstallment = null, showAmount = false),
        )
        val selection = options.first()
        val componentState = createComponentState(
            installmentState = InstallmentState(options, selection),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(options, viewState.installmentViewState?.installmentOptions)
        assertEquals(selection, viewState.installmentViewState?.selectedInstallment)
    }

    @Suppress("LongParameterList")
    private fun createComponentState(
        cardNumber: TextInputComponentState = TextInputComponentState(),
        cardBrandState: CardBrandState = CardBrandState.NoBrandsDetected,
        expiryDate: TextInputComponentState = TextInputComponentState(),
        securityCode: TextInputComponentState = TextInputComponentState(),
        postalCode: TextInputComponentState = TextInputComponentState(),
        supportedCardBrands: List<CardBrand> = emptyList(),
        showSupportedCardBrandLogos: Boolean = true,
        isCardScanningAvailable: Boolean = false,
        installmentState: InstallmentState = InstallmentState(emptyList(), null),
    ) = CardComponentState(
        cardNumber = cardNumber,
        expiryDate = expiryDate,
        securityCode = securityCode,
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        postalCode = postalCode,
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = supportedCardBrands,
        showSupportedCardBrandLogos = showSupportedCardBrandLogos,
        isLoading = false,
        isCardScanningAvailable = isCardScanningAvailable,
        cardBrandState = cardBrandState,
        networkBinLookupState = null,
        installmentState = installmentState,
    )

    private fun getCardBrandData(cardBrand: CardBrand = CardBrand("")): CardBrandData {
        return CardBrandData(
            cardBrand = cardBrand,
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }

    companion object {
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
