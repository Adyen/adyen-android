/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.google.android.gms.wallet.PaymentData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GooglePayComponentStateReducerTest {

    private lateinit var reducer: GooglePayComponentStateReducer

    @Mock
    private lateinit var paymentData: PaymentData

    @BeforeEach
    fun beforeEach() {
        reducer = GooglePayComponentStateReducer()
    }

    @Test
    fun `when intent is UpdateLoading, then isLoading is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, GooglePayIntent.UpdateLoading(true))

        assertTrue(actual.isLoading)
    }

    @Test
    fun `when intent is UpdateButtonVisible, then isButtonVisible is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, GooglePayIntent.UpdateButtonVisible(true))

        assertTrue(actual.isButtonVisible)
    }

    @Test
    fun `when intent is UpdatePaymentData, then paymentData is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, GooglePayIntent.UpdatePaymentData(paymentData))

        assertEquals(paymentData, actual.paymentData)
    }

    @Test
    fun `when intent is UpdateAvailability, then isAvailable is updated`() {
        val state = createInitialState()

        val actual = reducer.reduce(state, GooglePayIntent.UpdateAvailability(true))

        assertTrue(actual.isAvailable)
    }

    private fun createInitialState() = GooglePayComponentState(
        isButtonVisible = false,
        isLoading = false,
        isAvailable = false,
        paymentData = null,
    )
}
