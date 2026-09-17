/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 16/9/2026.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * The post processor runs after the validator, so every state here is written as it would be once validation has
 * run: a field that should count as unfilled carries an error.
 */
internal class CardComponentStatePostProcessorTest {

    private val postProcessor = CardComponentStatePostProcessor()

    @Test
    fun `when the initial state is created, then the first invalid field is asked for focus`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and the card number is invalid, then its error shows and it is asked for focus`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        assertTrue(actual.cardNumber.isErrorVisible)
        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when pay is pressed and nothing is invalid, then no error shows and no focus is requested`() {
        val state = createInitialState()

        val actual = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.isErrorVisible)
        assertFalse(actual.expiryDate.isErrorVisible)
        assertFalse(actual.securityCode.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when pay is pressed with several invalid fields, then the first one in the form gets focus`() {
        val state = createInitialState().copy(
            expiryDate = invalidField(),
            securityCode = invalidField(),
        )

        val actual = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    /**
     * The bug this whole mechanism exists for: the focus that pay asks for used to arrive as a plain focus gain, which
     * hid the error it had just revealed. The field flashed an error and lost it again.
     */
    @Test
    fun `when the field pay focused reports the focus gain, then it keeps showing its error`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        val actual = postProcessor.process(
            highlighted,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when the shopper taps a field showing an error, then the error is hidden`() {
        val state = createInitialState().copy(expiryDate = invalidField(isErrorVisible = true))

        val actual = postProcessor.process(
            state,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertFalse(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when an invalid field loses focus, then its error is shown`() {
        val state = createInitialState().copy(expiryDate = invalidField())

        val actual = postProcessor.process(
            state,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = false),
        )

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    /**
     * A focus change never clears the request, because the UI reports back even when focus did not move - which is
     * what happens when pay targets a field that already has focus.
     */
    @Test
    fun `when the requested field gains focus, then the request is left for the UI to report back`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        val actual = postProcessor.process(
            highlighted,
            CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, hasFocus = true),
        )

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when the UI reports the request back, then it is cleared`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        val actual = postProcessor.process(
            highlighted,
            CardIntent.FocusRequestConsumed(CardFormElementId.EXPIRY_DATE),
        )

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the UI reports back a request for another field, then the pending one is left alone`() {
        val state = createInitialState().copy(expiryDate = invalidField())
        val highlighted = postProcessor.process(state, CardIntent.HighlightValidationErrors)

        val actual = postProcessor.process(
            highlighted,
            CardIntent.FocusRequestConsumed(CardFormElementId.CARD_NUMBER),
        )

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, showErrorIfPresent = true), actual.focusRequest)
    }

    @Test
    fun `when a scan filled the card number and expiry date, then focus is requested on the security code`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = postProcessor.process(state, scanResult())

        assertEquals(FocusRequest(CardFormElementId.SECURITY_CODE), actual.focusRequest)
    }

    @Test
    fun `when a scan filled everything the form shows, then no focus is requested`() {
        val state = createInitialState()

        val actual = postProcessor.process(state, scanResult())

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the security code is hidden, then a scan sends focus to the next visible field`() {
        val state = createInitialState().copy(
            securityCode = hiddenField(),
            holderName = invalidField(),
        )

        val actual = postProcessor.process(state, scanResult())

        assertEquals(FocusRequest(CardFormElementId.HOLDER_NAME), actual.focusRequest)
    }

    /**
     * A scan can come back with only part of a card. Skipping to the field after the expiry date would then send the
     * shopper past a field the scan did not fill.
     */
    @Test
    fun `when a scan left the expiry date empty, then focus is requested on the expiry date`() {
        val state = createInitialState().copy(
            expiryDate = invalidField(),
            securityCode = invalidField(),
        )

        val actual = postProcessor.process(state, scanResult(expiryMonth = null, expiryYear = null))

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
    }

    /**
     * A scan without a card number also wipes the one the shopper had typed, so the card number is what needs filling
     * again. Reading only the expiry date and jumping forward to the security code would strand them.
     */
    @Test
    fun `when a scan left the card number empty, then focus is requested on the card number`() {
        val state = createInitialState().copy(
            cardNumber = invalidField(),
            securityCode = invalidField(),
        )

        val actual = postProcessor.process(state, scanResult(pan = null))

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    /**
     * Prefill behaves like a shopper tap, unlike the focus that pay asks for, so the field it lands on must not
     * surface an error the shopper has not seen yet.
     */
    @Test
    fun `when the field a scan focused was already showing an error, then the error is hidden on arrival`() {
        val state = createInitialState().copy(securityCode = invalidField(isErrorVisible = true))
        val scanned = postProcessor.process(state, scanResult())

        val actual = postProcessor.process(
            scanned,
            CardIntent.UpdateFieldFocus(CardFormElementId.SECURITY_CODE, hasFocus = true),
        )

        assertFalse(actual.securityCode.isErrorVisible)
    }

    @Test
    fun `when the intent decides no focus, then the state is untouched`() {
        val state = createInitialState().copy(cardNumber = invalidField())

        val actual = postProcessor.process(state, CardIntent.UpdateLoading(true))

        assertEquals(state, actual)
    }

    private fun scanResult(
        pan: String? = "4111111111111111",
        expiryMonth: Int? = 12,
        expiryYear: Int? = 2025,
    ) = CardIntent.UpdateCardScanResult(pan = pan, expiryMonth = expiryMonth, expiryYear = expiryYear)

    private fun invalidField(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE, isErrorVisible),
    )

    private fun hiddenField() = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden)

    private fun createInitialState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        postalCode = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        showSupportedCardBrandLogos = true,
        isLoading = false,
        isCardScanningAvailable = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
        networkBinLookupState = null,
        installmentState = InstallmentState(
            installmentOptions = emptyList(),
            selectedInstallment = null,
        ),
    )
}
