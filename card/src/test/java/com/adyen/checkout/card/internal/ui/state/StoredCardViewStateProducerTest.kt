/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/7/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class StoredCardViewStateProducerTest {

    private lateinit var producer: StoredCardViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = StoredCardViewStateProducer(amount = TEST_AMOUNT)
    }

    @Test
    fun `when produce is called, then amount is propagated to the view state`() {
        // GIVEN
        val componentState = createComponentState()

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(TEST_AMOUNT, viewState.amount)
    }

    @Test
    fun `when produce is called, then isLoading is propagated to the view state`() {
        // GIVEN
        val componentState = createComponentState(isLoading = true)

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(true, viewState.isLoading)
    }

    @Test
    fun `when detected card type is null, then brand is null and cardNumberFormat is Default`() {
        // GIVEN
        val componentState = createComponentState(detectedCardType = null)

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertNull(viewState.brand)
        assertEquals(CardNumberFormat.DEFAULT, viewState.cardNumberFormat)
    }

    @Test
    fun `when amex brand is detected, then brand is propagated and cardNumberFormat is Amex`() {
        // GIVEN
        val cardBrand = CardBrand("amex")
        val componentState = createComponentState(
            detectedCardType = getDetectedCardType(cardBrand),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(cardBrand, viewState.brand)
        assertEquals(CardNumberFormat.AMEX, viewState.cardNumberFormat)
    }

    @Test
    fun `when non-amex brand is detected, then brand is propagated and cardNumberFormat is Default`() {
        // GIVEN
        val cardBrand = CardBrand("visa")
        val componentState = createComponentState(
            detectedCardType = getDetectedCardType(cardBrand),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(cardBrand, viewState.brand)
        assertEquals(CardNumberFormat.DEFAULT, viewState.cardNumberFormat)
    }

    @Test
    fun `when security code is valid, then trailing icon is Checkmark`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "123",
                errorMessage = null,
                showError = false,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.Checkmark, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code has error, then trailing icon is Warning`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "12",
                errorMessage = CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID,
                showError = true,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.Warning, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code is empty and amex is detected, then trailing icon is PlaceholderAmex`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "",
                errorMessage = null,
                showError = false,
            ),
            detectedCardType = getDetectedCardType(CardBrand("amex")),
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
            securityCode = TextInputComponentState(
                text = "",
                errorMessage = null,
                showError = false,
            ),
            detectedCardType = getDetectedCardType(CardBrand("visa")),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderDefault, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when security code has error but showError is false, then trailing icon is placeholder`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(
                text = "12",
                errorMessage = CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID,
                showError = false,
            ),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals(SecurityCodeTrailingIcon.PlaceholderDefault, viewState.securityCode?.trailingIcon)
    }

    @Test
    fun `when produce is called, then security code text is propagated to the view state`() {
        // GIVEN
        val componentState = createComponentState(
            securityCode = TextInputComponentState(text = "737"),
        )

        // WHEN
        val viewState = producer.produce(componentState)

        // THEN
        assertEquals("737", viewState.securityCode?.text)
    }

    private fun createComponentState(
        securityCode: TextInputComponentState = TextInputComponentState(),
        isLoading: Boolean = false,
        detectedCardType: DetectedCardType? = null,
    ) = StoredCardComponentState(
        securityCode = securityCode,
        isLoading = isLoading,
        detectedCardType = detectedCardType,
    )

    private fun getDetectedCardType(cardBrand: CardBrand) = DetectedCardType(
        cardBrand = cardBrand,
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

    companion object {
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
