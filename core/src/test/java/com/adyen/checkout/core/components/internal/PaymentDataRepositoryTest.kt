/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/10/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.lifecycle.SavedStateHandle
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class PaymentDataRepositoryTest {

    @Test
    fun `when payment data is set, then a new repository on the same handle reads it`() {
        val savedStateHandle = SavedStateHandle()
        PaymentDataRepository(savedStateHandle).apply {
            paymentData = TEST_PAYMENT_DATA
            nativeRedirectData = TEST_NATIVE_REDIRECT_DATA
        }

        val restoredRepository = PaymentDataRepository(savedStateHandle)

        assertEquals(TEST_PAYMENT_DATA, restoredRepository.paymentData)
        assertEquals(TEST_NATIVE_REDIRECT_DATA, restoredRepository.nativeRedirectData)
    }

    @Test
    fun `when the handle holds values under unprefixed keys, then they are not read`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                UNPREFIXED_PAYMENT_DATA_KEY to OTHER_VALUE,
                UNPREFIXED_NATIVE_REDIRECT_DATA_KEY to OTHER_VALUE,
            ),
        )

        val repository = PaymentDataRepository(savedStateHandle)

        assertNull(repository.paymentData)
        assertNull(repository.nativeRedirectData)
    }

    @Test
    fun `when payment data is set, then values under unprefixed keys are not overwritten`() {
        val savedStateHandle = SavedStateHandle(
            mapOf(
                UNPREFIXED_PAYMENT_DATA_KEY to OTHER_VALUE,
                UNPREFIXED_NATIVE_REDIRECT_DATA_KEY to OTHER_VALUE,
            ),
        )

        PaymentDataRepository(savedStateHandle).apply {
            paymentData = TEST_PAYMENT_DATA
            nativeRedirectData = TEST_NATIVE_REDIRECT_DATA
        }

        assertEquals(OTHER_VALUE, savedStateHandle.get<String>(UNPREFIXED_PAYMENT_DATA_KEY))
        assertEquals(OTHER_VALUE, savedStateHandle.get<String>(UNPREFIXED_NATIVE_REDIRECT_DATA_KEY))
    }

    companion object {
        private const val TEST_PAYMENT_DATA = "test_payment_data"
        private const val TEST_NATIVE_REDIRECT_DATA = "test_native_redirect_data"
        private const val OTHER_VALUE = "other_value"
        private const val UNPREFIXED_PAYMENT_DATA_KEY = "payment_data"
        private const val UNPREFIXED_NATIVE_REDIRECT_DATA_KEY = "native_redirect_data"
    }
}
