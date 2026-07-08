/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 25/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.state

import com.google.android.gms.wallet.PaymentData
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
internal class GooglePayComponentStateValidatorTest {

    private lateinit var validator: GooglePayComponentStateValidator

    @Mock
    private lateinit var paymentData: PaymentData

    @BeforeEach
    fun beforeEach() {
        validator = GooglePayComponentStateValidator()
    }

    @Test
    fun `when google pay in unavailable, then isValid returns false`() {
        val state = createState(isAvailable = false)

        val result = validator.isValid(state)

        assertFalse(result)
    }

    @Test
    fun `when google pay in available, then isValid returns true`() {
        val state = createState(isAvailable = true)

        val result = validator.isValid(state)

        assertTrue(result)
    }

    @Test
    fun `when validate is called, then state is returned unchanged`() {
        val state = createState(isAvailable = true)

        val result = validator.validate(state)

        assertSame(state, result)
    }

    private fun createState(isAvailable: Boolean) = GooglePayComponentState(
        allowedPaymentMethods = "[]",
        buttonStyling = null,
        isButtonVisible = false,
        isLoading = false,
        isAvailable = isAvailable,
        paymentData = null,
    )
}
