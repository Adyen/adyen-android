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
import org.mockito.kotlin.whenever

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
    fun `when paymentData is null, then isValid returns false`() {
        val state = createState(paymentData = null)

        val result = validator.isValid(state)

        assertFalse(result)
    }

    @Test
    fun `when paymentData has valid token, then isValid returns true`() {
        whenever(paymentData.toJson()).thenReturn(VALID_PAYMENT_DATA_JSON)
        val state = createState(paymentData = paymentData)

        val result = validator.isValid(state)

        assertTrue(result)
    }

    @Test
    fun `when paymentData has empty token, then isValid returns false`() {
        whenever(paymentData.toJson()).thenReturn(EMPTY_TOKEN_PAYMENT_DATA_JSON)
        val state = createState(paymentData = paymentData)

        val result = validator.isValid(state)

        assertFalse(result)
    }

    @Test
    fun `when validate is called, then state is returned unchanged`() {
        val state = createState(paymentData = null)

        val result = validator.validate(state)

        assertSame(state, result)
    }

    private fun createState(paymentData: PaymentData?) = GooglePayComponentState(
        isButtonVisible = false,
        isLoading = false,
        isAvailable = false,
        paymentData = paymentData,
    )

    companion object {
        private const val VALID_PAYMENT_DATA_JSON = """
            {
                "paymentMethodData": {
                    "tokenizationData": {
                        "token": "test_token_123"
                    }
                }
            }
        """

        private const val EMPTY_TOKEN_PAYMENT_DATA_JSON = """
            {
                "paymentMethodData": {
                    "tokenizationData": {
                        "token": ""
                    }
                }
            }
        """
    }
}
