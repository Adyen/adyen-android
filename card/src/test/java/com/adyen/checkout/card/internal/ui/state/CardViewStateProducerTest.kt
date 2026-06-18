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
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.card.internal.ui.model.PostalCodeTrailingIcon
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardViewStateProducerTest {

    private lateinit var producer: CardViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = CardViewStateProducer()
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
        assertTrue(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertTrue(viewState.isSupportedCardBrandsShown)
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
        assertTrue(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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

    // UC6: Error Hides Brand Logos
    @Test
    fun `when card number has error with detected brand, then trailing icon is warning and supported brands are hidden`() {
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
            CardNumberTrailingIcon.Warning,
            viewState.cardNumber?.trailingIcon,
        )
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertTrue(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
            assertFalse(viewState.isSupportedCardBrandsShown)
            assertEquals(
                CardNumberTrailingIcon.BrandLogos,
                viewState.cardNumber?.trailingIcon,
            )
        }
    }

    @Test
    fun `when postal code has error, then trailing icon is warning`() {
        // GIVEN
        val componentState = createComponentState(
            postalCode = TextInputComponentState(
                text = "",
                errorMessage = CheckoutLocalizationKey.CARD_POSTAL_CODE_INVALID,
                showError = true,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(
            PostalCodeTrailingIcon.Warning,
            viewState.postalCode?.trailingIcon,
        )
        assertEquals(true, viewState.postalCode?.isError)
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
            viewState.supportedCardBrands,
        )
    }

    @Test
    fun `when postal code is valid, then trailing icon is placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            postalCode = TextInputComponentState(
                text = "1234 AB",
                errorMessage = null,
                showError = false,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(
            PostalCodeTrailingIcon.Placeholder,
            viewState.postalCode?.trailingIcon,
        )
        assertEquals(false, viewState.postalCode?.isError)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
        assertFalse(viewState.isSupportedCardBrandsShown)
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
    fun `when card scanning is available and card number has error, then trailing icon is Warning not ScanButton`() {
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
        assertEquals(CardNumberTrailingIcon.Warning, viewState.cardNumber?.trailingIcon)
    }

    @Test
    fun `when installment state has options and selection, then viewState contains same options and selection`() {
        // GIVEN
        val options = listOf(
            InstallmentModel.Regular(3, amountPerInstallment = null, showAmount = false)
        )
        val selection = options.first()
        val componentState = createComponentState(
            installmentState = InstallmentState(options, selection)
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
        postalCode: TextInputComponentState = TextInputComponentState(),
        supportedCardBrands: List<CardBrand> = emptyList(),
        showSupportedCardBrandLogos: Boolean = true,
        isCardScanningAvailable: Boolean = false,
        installmentState: InstallmentState = InstallmentState(emptyList(), null),
    ) = CardComponentState(
        cardNumber = cardNumber,
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
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

    private fun getCardBrandData(): CardBrandData {
        return CardBrandData(
            cardBrand = CardBrand(""),
            enableLuhnCheck = true,
            cvcPolicy = Brand.FieldPolicy.REQUIRED,
            expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
            panLength = null,
            paymentMethodVariant = null,
            localizedBrand = null,
        )
    }
}
