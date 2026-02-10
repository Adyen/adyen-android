/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by andriim on 22/1/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.DualBrandedCardHandler
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
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
        producer = CardViewStateProducer(
            dualBrandedCardHandler = DualBrandedCardHandler(),
        )
    }

    // UC5: Brand Detection Hides Placeholder (No Error)
    @Test
    fun `when no card brand is detected, then supported card brands should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            detectedCardTypes = emptyList(),
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
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
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
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("unknown"),
                    isSupported = false,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertTrue(viewState.isSupportedCardBrandsShown)
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
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(
            CardNumberTrailingIcon.Warning,
            viewState.cardNumber.trailingIcon
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
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.cardNumber.isError)
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
            detectedCardTypes = emptyList(),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.cardNumber.isError)
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
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.isSupportedCardBrandsShown)
        assertFalse(viewState.cardNumber.isError)
        assertEquals(
            CardNumberTrailingIcon.BrandLogos,
            viewState.cardNumber.trailingIcon
        )
    }

    // UC9: Partial Input While Typing (No Validation)
    @Test
    fun `when user is typing with partial input, then no error should be shown`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                errorMessage = CheckoutLocalizationKey.CARD_NUMBER_INVALID,
                isFocused = true,
                showError = false, // Text change sets showError to false
            ),
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.cardNumber.isError)
    }

    // UC9: Partial Input While Typing - verify brand logos hidden when brand detected
    @Test
    fun `when user is typing with partial input and brand is detected, then supported brands are hidden and trailing icon is brand logos`() {
        // GIVEN
        val componentState = createComponentState(
            cardNumber = TextInputComponentState(
                text = "4111",
                isFocused = true,
                showError = false,
            ),
            detectedCardTypes = listOf(
                createDetectedCardType(
                    cardBrand = CardBrand("visa"),
                    isSupported = true,
                ),
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertFalse(viewState.isSupportedCardBrandsShown)
        assertEquals(
            CardNumberTrailingIcon.BrandLogos,
            viewState.cardNumber.trailingIcon
        )
    }

    private fun createComponentState(
        cardNumber: TextInputComponentState = TextInputComponentState(),
        detectedCardTypes: List<DetectedCardType> = emptyList(),
    ) = CardComponentState(
        cardNumber = cardNumber,
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        detectedCardTypes = detectedCardTypes,
        selectedCardBrand = null,
    )

    private fun createDetectedCardType(
        cardBrand: CardBrand = CardBrand("visa"),
        isSupported: Boolean = true,
    ) = DetectedCardType(
        cardBrand = cardBrand,
        isReliable = true,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = isSupported,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}
