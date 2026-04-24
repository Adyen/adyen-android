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
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
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
    fun `when intent is UpdateCardNumberFocus, then cardNumber focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateCardNumberFocus(true))

        assertTrue(actual.cardNumber.isFocused)
    }

    @Test
    fun `when intent is UpdateExpiryDate, then expiryDate state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDate("1225"))

        assertEquals("1225", actual.expiryDate.text)
    }

    @Test
    fun `when intent is UpdateExpiryDateFocus, then expiryDate focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateExpiryDateFocus(true))

        assertTrue(actual.expiryDate.isFocused)
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateSecurityCodeFocus, then securityCode focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSecurityCodeFocus(true))

        assertTrue(actual.securityCode.isFocused)
    }

    @Test
    fun `when intent is UpdateHolderName, then holderName state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderName("John Doe"))

        assertEquals("John Doe", actual.holderName.text)
    }

    @Test
    fun `when intent is UpdateHolderNameFocus, then holderName focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateHolderNameFocus(true))

        assertTrue(actual.holderName.isFocused)
    }

    @Test
    fun `when intent is UpdateSocialSecurityNumber, then socialSecurityNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSocialSecurityNumber("123456"))

        assertEquals("123456", actual.socialSecurityNumber.text)
    }

    @Test
    fun `when intent is UpdateSocialSecurityNumberFocus, then socialSecurityNumber focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateSocialSecurityNumberFocus(true))

        assertTrue(actual.socialSecurityNumber.isFocused)
    }

    @Test
    fun `when intent is UpdateKcpBirthDateOrTaxNumber, then kcpBirthDateOrTaxNumber state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpBirthDateOrTaxNumber("123456"))

        assertEquals("123456", actual.kcpBirthDateOrTaxNumber.text)
    }

    @Test
    fun `when intent is UpdateKcpBirthDateOrTaxNumberFocus, then kcpBirthDateOrTaxNumber focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpBirthDateOrTaxNumberFocus(true))

        assertTrue(actual.kcpBirthDateOrTaxNumber.isFocused)
    }

    @Test
    fun `when intent is UpdateKcpCardPassword, then kcpCardPassword state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpCardPassword("123456"))

        assertEquals("123456", actual.kcpCardPassword.text)
    }

    @Test
    fun `when intent is UpdateKcpCardPasswordFocus, then kcpCardPassword focus is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateKcpCardPasswordFocus(true))

        assertTrue(actual.kcpCardPassword.isFocused)
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
    fun `when intent is HighlightValidationErrors and cardNumber has error, then cardNumber showError and focus are set`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(
                text = "",
                isFocused = false,
                errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE,
                showError = false,
            ),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertTrue(actual.cardNumber.showError)
        assertTrue(actual.cardNumber.isFocused)
    }

    @Test
    fun `when intent is HighlightValidationErrors and no errors, then no fields are highlighted`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.showError)
        assertFalse(actual.expiryDate.showError)
        assertFalse(actual.securityCode.showError)
        assertFalse(actual.holderName.showError)
    }

    @Test
    fun `when intent is HighlightValidationErrors with multiple errors, then first field with error gets focus`() {
        val state = createInitialState().copy(
            cardNumber = TextInputComponentState(errorMessage = null),
            expiryDate = TextInputComponentState(errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE),
            securityCode = TextInputComponentState(errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE),
        )

        val actual = reducer.reduce(state, CardIntent.HighlightValidationErrors)

        assertFalse(actual.cardNumber.isFocused)
        assertTrue(actual.expiryDate.isFocused)
        assertFalse(actual.securityCode.isFocused)
    }

    private fun createInitialState() = CardComponentState(
        cardNumber = TextInputComponentState(),
        expiryDate = TextInputComponentState(),
        securityCode = TextInputComponentState(),
        holderName = TextInputComponentState(),
        socialSecurityNumber = TextInputComponentState(),
        kcpCardPassword = TextInputComponentState(),
        kcpBirthDateOrTaxNumber = TextInputComponentState(),
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        cardBrandState = CardBrandState.NoBrandsDetected,
    )
}
