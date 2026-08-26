/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class GenericViewStateProducerTest {

    private lateinit var producer: GenericViewStateProducer

    @BeforeEach
    fun beforeEach() {
        producer = GenericViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = true)
    }

    @Test
    fun `when produce is called, then view state is created`() {
        // GIVEN
        val componentState = GenericComponentState(isLoading = false)

        // WHEN
        val actual = producer.produce(componentState)

        // THEN
        val expected = GenericViewState(
            isLoading = false,
            payButtonViewState = PayButtonViewState(amount = TEST_AMOUNT, isLoading = false),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when state is loading, then loading is propagated to the view state and the pay button`() {
        // GIVEN
        val componentState = GenericComponentState(isLoading = true)

        // WHEN
        val actual = producer.produce(componentState)

        // THEN
        val expected = GenericViewState(
            isLoading = true,
            payButtonViewState = PayButtonViewState(amount = TEST_AMOUNT, isLoading = true),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `when show submit button is false, then pay button view state is null`() {
        // GIVEN
        val producer = GenericViewStateProducer(amount = TEST_AMOUNT, showSubmitButton = false)
        val componentState = GenericComponentState(isLoading = false)

        // WHEN
        val actual = producer.produce(componentState)

        // THEN
        assertNull(actual.payButtonViewState)
    }

    @Test
    fun `when amount is null, then pay button view state carries a null amount`() {
        // GIVEN
        val producer = GenericViewStateProducer(amount = null, showSubmitButton = true)
        val componentState = GenericComponentState(isLoading = false)

        // WHEN
        val actual = producer.produce(componentState)

        // THEN
        assertEquals(PayButtonViewState(amount = null, isLoading = false), actual.payButtonViewState)
    }

    companion object {
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1337)
    }
}
