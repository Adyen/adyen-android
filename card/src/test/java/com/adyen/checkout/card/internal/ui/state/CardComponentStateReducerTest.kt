/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.DetectCardTypeBinHelper
import com.adyen.checkout.card.internal.ui.model.CardComponentParams
import com.adyen.checkout.card.internal.ui.model.InstallmentModel
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock

@ExtendWith(MockitoExtension::class)
internal class CardComponentStateReducerTest {

    private lateinit var reducer: CardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        val detectCardTypeBinHelper = DetectCardTypeBinHelper()
        val cardComponentParams = mock<CardComponentParams>()
        val cardBrandIntentsHandler = CardBrandIntentsHandler(cardComponentParams, detectCardTypeBinHelper)
        reducer = CardComponentStateReducer(cardBrandIntentsHandler)
    }

    @Test
    fun `when intent is UpdateCardNumber, then cardNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardNumber("4111111111111111"))

        assertEquals("4111111111111111", actual.cardNumber.text)
    }

    @Test
    fun `when intent is UpdateExpiryDate, then expiryDate state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDate("1225"))

        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateHolderName, then holderName state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderName("John Doe"))

        assertEquals("John Doe", actual.holderName.text)
    }

    @Test
    fun `when intent is UpdateSocialSecurityNumber, then socialSecurityNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSocialSecurityNumber("123456"))

        assertEquals("123456", actual.socialSecurityNumber.text)
    }

    @Test
    fun `when intent is UpdateKcpBirthDateOrTaxNumber, then kcpBirthDateOrTaxNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpBirthDateOrTaxNumber("123456"))

        assertEquals("123456", actual.kcpBirthDateOrTaxNumber.text)
    }

    @Test
    fun `when intent is UpdateKcpCardPassword, then kcpCardPassword state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpCardPassword("123456"))

        assertEquals("123456", actual.kcpCardPassword.text)
    }

    @Test
    fun `when intent is UpdatePostalCode, then postalCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdatePostalCode("1234 AB"))

        assertEquals("1234 AB", actual.postalCode.text)
    }

    @Test
    fun `when intent is UpdateStorePaymentMethod, then storePaymentMethod is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateStorePaymentMethod(true))

        assertTrue(actual.storePaymentMethod)
    }

    @Test
    fun `when intent is UpdateLoading, then isLoading is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is HighlightValidationErrors and cardNumber has error, then the cardNumber error is shown and focus is set`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(
                text = "",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE)
            ),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertTrue(actual.cardNumber.isErrorVisible)
        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when intent is HighlightValidationErrors and no errors, then no fields are highlighted`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.isErrorVisible)
        assertFalse(actual.expiryDate.isErrorVisible)
        assertFalse(actual.securityCode.isErrorVisible)
        assertFalse(actual.holderName.isErrorVisible)
        assertFalse(actual.postalCode.isErrorVisible)
    }

    @Test
    fun `when intent is HighlightValidationErrors with multiple errors, then first field with error gets focus`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(error = null),
            expiryDate = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE)
            ),
            securityCode = TextInputComponentState(
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE)
            ),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when intent is HighlightValidationErrors, then the first invalid field is asked to keep its error`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = hiddenError()),
            securityCode = TextInputComponentState(error = hiddenError()),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when intent is HighlightValidationErrors and no errors, then no focus is requested`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertNull(actual.focusRequest)
    }

    /**
     * The bug this whole mechanism exists for: the focus that pay asks for used to arrive as a plain focus gain, which
     * hid the error it had just revealed. The field flashed an error and lost it again.
     */
    @Test
    fun `when the field pay focused reports the focus gain, then it keeps showing its error`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = hiddenError()),
        )
        val highlighted = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        val actual = reducer.reduce(highlighted, CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, true))

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when the shopper taps a field showing an error, then the error is hidden`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = visibleError()),
        )

        val actual = reducer.reduce(state, CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, true))

        assertFalse(actual.expiryDate.isErrorVisible)
    }

    @Test
    fun `when an invalid field loses focus, then its error is shown`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = hiddenError()),
        )

        val actual = reducer.reduce(state, CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, false))

        assertTrue(actual.expiryDate.isErrorVisible)
    }

    /**
     * Without this the request would outlive the focus move it asked for, and the shopper's next tap on the same field
     * would be read as programmatic and keep an error it should clear.
     */
    @Test
    fun `when the requested field gains focus, then the request is cleared`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = hiddenError()),
        )
        val highlighted = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        val focused = reducer.reduce(highlighted, CardIntent.UpdateFieldFocus(CardFormElementId.EXPIRY_DATE, true))

        assertNull(focused.focusRequest)
    }

    @Test
    fun `when a field other than the requested one gains focus, then the request is left alone`() {
        val state = createInitialState().copy(
            expiryDate = TextInputComponentState(error = hiddenError()),
        )
        val highlighted = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        val actual = reducer.reduce(highlighted, CardIntent.UpdateFieldFocus(CardFormElementId.CARD_NUMBER, true))

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE, keepErrorHighlight = true), actual.focusRequest)
    }

    @Test
    fun `when intent is UpdateCardScanningAvailability with true, then isCardScanningAvailable is true`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardScanningAvailability(true))

        assertTrue(actual.isCardScanningAvailable)
    }

    @Test
    fun `when intent is UpdateCardScanningAvailability with false, then isCardScanningAvailable is false`() {
        val state = createInitialState().copy(isCardScanningAvailable = true)

        val actual = reducer.reduce(state, CardIntent.UpdateCardScanningAvailability(false))

        assertFalse(actual.isCardScanningAvailable)
    }

    @Test
    fun `when intent is UpdateCardScanResult with pan and expiry, then cardNumber and expiryDate are updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        assertEquals("4111111111111111", actual.cardNumber.text)
        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateCardScanResult with null pan, then cardNumber is empty`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = null, expiryMonth = 3, expiryYear = 2026),
        )

        assertEquals("", actual.cardNumber.text)
        assertEquals("0326", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateCardScanResult with null expiry, then expiryDate is empty`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "5500000000000004", expiryMonth = null, expiryYear = null),
        )

        assertEquals("5500000000000004", actual.cardNumber.text)
        assertEquals("", actual.expiryDate.text)
    }

    @Test
    fun `when a card is scanned, then focus is requested on the field after the expiry date`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        assertEquals(FocusRequest(CardFormElementId.SECURITY_CODE), actual.focusRequest)
    }

    @Test
    fun `when a card is scanned and the security code is hidden, then focus is requested on the next visible field`() {
        val state = createInitialState().copy(
            securityCode = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        )

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        assertEquals(FocusRequest(CardFormElementId.HOLDER_NAME), actual.focusRequest)
    }

    /**
     * A scan can come back with only part of a card. Skipping to the field after the expiry date would then send the
     * shopper past a field the scan did not fill.
     */
    @Test
    fun `when a scan returns only a card number, then focus is requested on the expiry date`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = null, expiryYear = null),
        )

        assertEquals(FocusRequest(CardFormElementId.EXPIRY_DATE), actual.focusRequest)
    }

    /**
     * A scan without a card number also wipes the one the shopper had typed, so the card number is what needs filling
     * again. Reading only the expiry date and jumping forward to the security code would strand them.
     */
    @Test
    fun `when a scan returns no card number, then focus is requested on the card number`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = null, expiryMonth = 12, expiryYear = 2025),
        )

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    @Test
    fun `when a scan returns nothing usable, then focus is requested on the card number`() {
        val state = createInitialState()

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = null, expiryMonth = null, expiryYear = null),
        )

        assertEquals(FocusRequest(CardFormElementId.CARD_NUMBER), actual.focusRequest)
    }

    @Test
    fun `when a card is scanned and nothing follows the expiry date, then no focus is requested`() {
        val state = createInitialState().copy(
            securityCode = hiddenField(),
            holderName = hiddenField(),
            socialSecurityNumber = hiddenField(),
            kcpBirthDateOrTaxNumber = hiddenField(),
            kcpCardPassword = hiddenField(),
            postalCode = hiddenField(),
        )

        val actual = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        assertNull(actual.focusRequest)
    }

    /**
     * Prefill behaves like a shopper tap, unlike the focus that pay asks for, so the field it lands on must not
     * surface an error the shopper has not seen yet.
     */
    @Test
    fun `when the field a scan focused was already showing an error, then the error is hidden on arrival`() {
        val state = createInitialState().copy(
            securityCode = TextInputComponentState(error = visibleError()),
        )
        val scanned = reducer.reduce(
            state,
            CardIntent.UpdateCardScanResult(pan = "4111111111111111", expiryMonth = 12, expiryYear = 2025),
        )

        val actual = reducer.reduce(scanned, CardIntent.UpdateFieldFocus(CardFormElementId.SECURITY_CODE, true))

        assertFalse(actual.securityCode.isErrorVisible)
    }

    @Test
    fun `when intent is UpdateInstallment, then selectedInstallment is updated`() {
        val state = createInitialState()
        val installment = InstallmentModel.Regular(
            numberOfInstallments = 3,
            amountPerInstallment = null,
            showAmount = false,
        )

        val actual = reducer.reduce(state, CardIntent.UpdateInstallment(installment))

        assertEquals(installment, actual.installmentState.selectedInstallment)
    }

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

    private fun hiddenField() = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden)

    private fun hiddenError() = TextInputComponentState.InputError(CheckoutLocalizationKey.GENERAL_CLOSE)

    private fun visibleError() = TextInputComponentState.InputError(
        message = CheckoutLocalizationKey.GENERAL_CLOSE,
        isVisible = true,
    )
}
