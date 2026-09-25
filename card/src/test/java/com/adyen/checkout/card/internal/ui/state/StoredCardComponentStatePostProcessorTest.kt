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
import com.adyen.checkout.core.components.internal.ui.state.model.isErrorVisible
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

/**
 * The post processor runs after the validator, so every state here is written as it would be once validation has
 * run: a field that should count as unfilled carries an error.
 */
internal class StoredCardComponentStatePostProcessorTest {

    private val postProcessor = StoredCardComponentStatePostProcessor()

    @Test
    fun `when the initial state is created, then the security code is asked for focus`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = postProcessor.processInitialState(state)

        assertEquals(FocusRequest(StoredCardFormElementId.SECURITY_CODE), actual.focusRequest)
    }

    @Test
    fun `when the stored card asks for no security code, then nothing is asked for focus`() {
        val state = createInitialState().copy(
            securityCode = TextInputComponentState(requirementPolicy = RequirementPolicy.Hidden),
        )

        val actual = postProcessor.processInitialState(state)

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the security code loses focus, then an error it was holding back is shown`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = process(
            state,
            StoredCardIntent.UpdateFieldFocus(StoredCardFormElementId.SECURITY_CODE, hasFocus = false),
        )

        assertTrue(actual.securityCode.isErrorVisible)
    }

    @Test
    fun `when the shopper focuses the security code, then a visible error is hidden`() {
        val state = createInitialState().copy(securityCode = invalidField(isErrorVisible = true))

        val actual = process(
            state,
            StoredCardIntent.UpdateFieldFocus(StoredCardFormElementId.SECURITY_CODE, hasFocus = true),
        )

        assertFalse(actual.securityCode.isErrorVisible)
    }

    @Test
    fun `when pay is pressed and the security code is invalid, then the error is shown and focus requested`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = process(state, StoredCardIntent.HighlightValidationErrors)

        assertTrue(actual.securityCode.isErrorVisible)
        assertEquals(
            FocusRequest(StoredCardFormElementId.SECURITY_CODE, showErrorIfPresent = true),
            actual.focusRequest,
        )
    }

    @Test
    fun `when pay is pressed and nothing is invalid, then the security code is not highlighted`() {
        val state = createInitialState()

        val actual = process(state, StoredCardIntent.HighlightValidationErrors)

        assertFalse(actual.securityCode.isErrorVisible)
        assertNull(actual.focusRequest)
    }

    @Test
    fun `when a focus request could not be fulfilled, then reporting it back clears it`() {
        val state = createInitialState().copy(
            focusRequest = FocusRequest(StoredCardFormElementId.SECURITY_CODE),
        )

        val actual = process(
            state,
            StoredCardIntent.FocusRequestConsumed(StoredCardFormElementId.SECURITY_CODE),
        )

        assertNull(actual.focusRequest)
    }

    @Test
    fun `when the intent decides no focus, then the state is untouched`() {
        val state = createInitialState().copy(securityCode = invalidField())

        val actual = process(state, StoredCardIntent.UpdateLoading(true))

        assertEquals(state, actual)
    }

    private fun process(state: StoredCardComponentState, intent: StoredCardIntent) =
        postProcessor.process(state, state, intent)

    private fun invalidField(isErrorVisible: Boolean = false) = TextInputComponentState(
        error = TextInputComponentState.InputError(
            CheckoutLocalizationKey.CARD_SECURITY_CODE_INVALID,
            isErrorVisible,
        ),
    )

    private fun createInitialState() = StoredCardComponentState(
        securityCode = TextInputComponentState(),
        isLoading = false,
        detectedCardType = null,
    )
}
