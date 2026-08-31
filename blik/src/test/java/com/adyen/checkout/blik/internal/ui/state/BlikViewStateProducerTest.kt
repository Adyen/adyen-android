/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.form.FocusRequest
import com.adyen.checkout.core.components.internal.ui.state.form.KeyboardAction
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull

internal class BlikViewStateProducerTest {

    private lateinit var producer: BlikViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = BlikViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = true)
    }

    @Test
    fun `when produce is called, then view state is created`() {
        val componentState = BlikComponentState(
            blikCode = TextInputComponentState(
                text = "123456",
                error = TextInputComponentState.InputError(CheckoutLocalizationKey.BLIK_CODE_INVALID, isVisible = true)
            ),
            isLoading = true,
        )

        val actual = producer.produce(componentState)

        val expected = BlikViewState(
            elements = listOf(
                BlikFormElement.BlikCode(
                    TextInputViewState(
                        text = "123456",
                        supportingText = CheckoutLocalizationKey.BLIK_CODE_INVALID,
                        isError = true,
                        // The only text input of the form, so it closes the keyboard rather than moving on.
                        keyboardAction = KeyboardAction.DONE,
                    ),
                ),
            ),
            isLoading = true,
            payButtonViewState = PayButtonViewState(TEST_AMOUNT, true),
        )

        assertEquals(expected, actual)
    }

    @Test
    fun `when the form asks the blik code for focus, then its element carries the request`() {
        val componentState = BlikComponentState(
            blikCode = TextInputComponentState(),
            isLoading = false,
            focusRequest = FocusRequest(BlikFieldId.BLIK_CODE),
        )

        val actual = producer.produce(componentState)

        val element = actual.elements.filterIsInstance<BlikFormElement.BlikCode>().single()
        assertNotNull(element.textInputViewState.focusRequest)
    }

    @Test
    fun `when show submit button is false then pay button view state is null`() {
        val producer = BlikViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = false)

        val actual = producer.produce(BlikComponentState(blikCode = TextInputComponentState(), isLoading = false))

        assertNull(actual.payButtonViewState)
    }

    companion object {
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
