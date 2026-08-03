/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 3/8/2026.
 */

package com.adyen.checkout.core.error

import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.core.error.internal.InvalidConfigurationError
import com.adyen.checkout.core.error.internal.PaymentMethodUnavailableError
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

internal class CheckoutErrorMapperTest {

    @Test
    fun `when error is GenericError, then code is GENERIC`() {
        val error = GenericError("generic message")

        val result = error.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.GENERIC, result.code)
        assertEquals("generic message", result.message)
        assertSame(error, result.cause)
    }

    @Test
    fun `when error is InvalidConfigurationError, then code is INVALID_CONFIGURATION`() {
        val error = InvalidConfigurationError("configuration message")

        val result = error.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.INVALID_CONFIGURATION, result.code)
        assertEquals("configuration message", result.message)
        assertSame(error, result.cause)
    }

    @Test
    fun `when error is PaymentMethodUnavailableError, then code is PAYMENT_METHOD_FAILURE`() {
        val error = PaymentMethodUnavailableError("unavailable message")

        val result = error.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.PAYMENT_METHOD_FAILURE, result.code)
        assertEquals("unavailable message", result.message)
        assertSame(error, result.cause)
    }

    @Test
    fun `when error is HttpError, then code is HTTP`() {
        val error = HttpError(code = 422, message = "http message", errorBody = null)

        val result = error.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.HTTP, result.code)
        assertEquals("http message", result.message)
        assertSame(error, result.cause)
    }

    @Test
    fun `when throwable is an InternalCheckoutError, then it delegates to the internal mapping`() {
        // The Throwable type is required, without it the call resolves to the InternalCheckoutError
        // overload and the delegation branch under test is never executed.
        val throwable: Throwable = InvalidConfigurationError("configuration message")

        val result = throwable.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.INVALID_CONFIGURATION, result.code)
    }

    @Test
    fun `when throwable is not an InternalCheckoutError, then code is GENERIC`() {
        val throwable = IllegalStateException("boom")

        val result = throwable.toCheckoutError()

        assertEquals(CheckoutError.ErrorCode.GENERIC, result.code)
        assertEquals("boom", result.message)
        assertSame(throwable, result.cause)
    }
}
