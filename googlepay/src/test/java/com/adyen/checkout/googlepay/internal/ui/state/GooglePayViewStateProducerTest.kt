/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/7/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class GooglePayViewStateProducerTest {

    @Test
    fun `when google pay is available and button is visible then button view state is created`() {
        val producer = GooglePayViewStateProducer(showSubmitButton = true)
        val state = createState(
            isAvailable = true,
            isButtonVisible = true,
            isLoading = true,
        )

        val actual = producer.produce(state)

        val expected = GooglePayButtonViewState(
            allowedPaymentMethods = ALLOWED_PAYMENT_METHODS,
            buttonStyling = null,
            isLoading = true,
        )
        assertEquals(expected, actual.payButtonViewState)
    }

    @Test
    fun `when show submit button is false then button view state is null`() {
        val producer = GooglePayViewStateProducer(showSubmitButton = false)
        val state = createState(
            isAvailable = true,
            isButtonVisible = true,
        )

        val actual = producer.produce(state)

        assertNull(actual.payButtonViewState)
    }

    @Test
    fun `when google pay is unavailable then button view state is null`() {
        val producer = GooglePayViewStateProducer(showSubmitButton = true)
        val state = createState(
            isAvailable = false,
            isButtonVisible = true,
        )

        val actual = producer.produce(state)

        assertNull(actual.payButtonViewState)
    }

    @Test
    fun `when button is not visible then button view state is null`() {
        val producer = GooglePayViewStateProducer(showSubmitButton = true)
        val state = createState(
            isAvailable = true,
            isButtonVisible = false,
        )

        val actual = producer.produce(state)

        assertNull(actual.payButtonViewState)
    }

    private fun createState(
        isAvailable: Boolean = true,
        isButtonVisible: Boolean = true,
        isLoading: Boolean = false,
    ) = GooglePayComponentState(
        allowedPaymentMethods = ALLOWED_PAYMENT_METHODS,
        buttonStyling = null,
        isButtonVisible = isButtonVisible,
        isLoading = isLoading,
        isAvailable = isAvailable,
    )

    companion object {
        private const val ALLOWED_PAYMENT_METHODS = "[]"
    }
}
