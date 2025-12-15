/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class CardComponentStateReducerTest {

    private lateinit var reducer: CardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = CardComponentStateReducer()
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
    fun `when intent is UpdateStorePaymentMethod, then storePaymentMethod is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, CardIntent.UpdateStorePaymentMethod(true))

        assertTrue(actual.storePaymentMethod)
    }

    @Test
    fun `when intent is SelectBrand, then selectedCardBrand is updated`() {
        val state = createInitialState()
        val cardBrand = CardBrand("visa")

        val actual = reducer.reduce(state, CardIntent.SelectBrand(cardBrand))

        assertEquals(cardBrand, actual.selectedCardBrand)
    }

    @Test
    fun `when intent is UpdateDetectedCardTypes, then detectedCardTypes is updated`() {
        val state = createInitialState()
        val detectedCardTypes = listOf(
            DetectedCardType(
                cardBrand = CardBrand("visa"),
                isReliable = true,
                enableLuhnCheck = true,
                cvcPolicy = Brand.FieldPolicy.REQUIRED,
                expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
                isSupported = true,
                panLength = 16,
                paymentMethodVariant = null,
                localizedBrand = null,
            ),
        )

        val actual = reducer.reduce(state, CardIntent.UpdateDetectedCardTypes(detectedCardTypes))

        assertEquals(detectedCardTypes, actual.detectedCardTypes)
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
        isHolderNameRequired = false,
        storePaymentMethod = false,
        isStorePaymentFieldVisible = false,
        supportedCardBrands = emptyList(),
        isLoading = false,
        detectedCardTypes = emptyList(),
        selectedCardBrand = null,
    )
}
