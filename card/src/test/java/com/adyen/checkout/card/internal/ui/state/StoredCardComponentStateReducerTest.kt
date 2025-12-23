/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
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

internal class StoredCardComponentStateReducerTest {

    private lateinit var reducer: StoredCardComponentStateReducer

    @BeforeEach
    fun beforeEach() {
        reducer = StoredCardComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateSecurityCode, then securityCode state is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateSecurityCode("123"))

        assertEquals("123", actual.securityCode.text)
    }

    @Test
    fun `when intent is UpdateSecurityCodeFocus with true, then securityCode focus is set to true`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateSecurityCodeFocus(true))

        assertTrue(actual.securityCode.isFocused)
    }

    @Test
    fun `when intent is UpdateSecurityCodeFocus with false, then securityCode focus is set to false`() {
        val state = createInitialState().copy(
            securityCode = TextInputComponentState(isFocused = true),
        )

        val actual = reducer.reduce(state, StoredCardIntent.UpdateSecurityCodeFocus(false))

        assertFalse(actual.securityCode.isFocused)
    }

    @Test
    fun `when intent is UpdateDetectedCardType, then detectedCardType is updated`() {
        val state = createInitialState()
        val detectedCardType = createDetectedCardType()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateDetectedCardType(detectedCardType))

        assertEquals(detectedCardType, actual.detectedCardType)
    }

    @Test
    fun `when intent is UpdateLoading with true, then isLoading is set to true`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is UpdateLoading with false, then isLoading is set to false`() {
        val state = createInitialState().copy(isLoading = true)

        val actual = reducer.reduce(state, StoredCardIntent.UpdateLoading(false))

        assertFalse(actual.isLoading)
    }

    @Test
    fun `when intent is HighlightValidationErrors and securityCode has error, then securityCode showError and focus are set`() {
        val state = createInitialState().copy(
            securityCode = TextInputComponentState(
                text = "",
                isFocused = false,
                errorMessage = CheckoutLocalizationKey.GENERAL_CLOSE,
                showError = false,
            ),
        )

        val actual = reducer.reduce(state, StoredCardIntent.HighlightValidationErrors)

        assertTrue(actual.securityCode.showError)
        assertTrue(actual.securityCode.isFocused)
    }

    @Test
    fun `when intent is HighlightValidationErrors and no errors, then securityCode is not highlighted`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, StoredCardIntent.HighlightValidationErrors)

        assertFalse(actual.securityCode.showError)
        assertFalse(actual.securityCode.isFocused)
    }

    private fun createInitialState() = StoredCardComponentState(
        securityCode = TextInputComponentState(),
        isLoading = false,
        detectedCardType = null,
    )

    private fun createDetectedCardType() = DetectedCardType(
        cardBrand = CardBrand("visa"),
        isReliable = true,
        enableLuhnCheck = true,
        cvcPolicy = Brand.FieldPolicy.REQUIRED,
        expiryDatePolicy = Brand.FieldPolicy.REQUIRED,
        isSupported = true,
        panLength = 16,
        paymentMethodVariant = null,
        localizedBrand = null,
    )
}
