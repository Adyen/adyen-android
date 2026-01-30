/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/8/2025.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.PaymentResult
import com.adyen.checkout.core.components.BeforeSubmitCallback
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.OnAdditionalDetailsCallback
import com.adyen.checkout.core.components.OnErrorCallback
import com.adyen.checkout.core.components.OnFinishedCallback
import com.adyen.checkout.core.components.OnSubmitCallback
import com.adyen.checkout.core.components.paymentmethod.TestPaymentComponentState
import com.adyen.checkout.core.error.CheckoutError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
class SessionsComponentCallbacksTest(
    @Mock private val beforeSubmitCallback: BeforeSubmitCallback,
    @Mock private val onErrorCallback: OnErrorCallback,
    @Mock private val onFinishedCallback: OnFinishedCallback,
    @Mock private val onSubmitCallback: OnSubmitCallback,
    @Mock private val onAdditionalDetailsCallback: OnAdditionalDetailsCallback,
) {

    @Test
    fun `given checkout callbacks, where onError is not set, an error should be thrown `() {
        val checkoutCallbacks = CheckoutCallbacks(
            beforeSubmit = { false },
            onFinished = {},
        )

        val exception = assertThrows<IllegalStateException> {
            checkoutCallbacks.toSessionsComponentCallbacks()
        }
        assertEquals("onError() callback is not set.", exception.message)
    }

    @Test
    fun `when beforeSubmit is set in checkout callbacks, beforeSubmit in sessions component triggers the set callback`() =
        runTest {
            val checkoutCallbacks = CheckoutCallbacks(
                beforeSubmit = beforeSubmitCallback,
                onSubmit = onSubmitCallback,
                onAdditionalDetails = onAdditionalDetailsCallback,
                onError = onErrorCallback,
            )

            val actualSessionsComponentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks()

            actualSessionsComponentCallbacks.beforeSubmit(TEST_PAYMENT_COMPONENT_STATE)
            verify(checkoutCallbacks.beforeSubmit)?.beforeSubmit(TEST_PAYMENT_COMPONENT_STATE)
        }

    @Test
    fun `when onSubmit is set in checkout callbacks, onSubmit in sessions component triggers the set callback`() =
        runTest {
            val checkoutCallbacks = CheckoutCallbacks(
                onSubmit = onSubmitCallback,
                onAdditionalDetails = onAdditionalDetailsCallback,
                onError = onErrorCallback,
            )

            whenever(onSubmitCallback.onSubmit(any())) doReturn CheckoutResult.Finished()

            val actualSessionsComponentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks()

            actualSessionsComponentCallbacks.onSubmit(TEST_PAYMENT_COMPONENT_STATE)
            verify(checkoutCallbacks.onSubmit)?.onSubmit(TEST_PAYMENT_COMPONENT_STATE)
        }

    @Test
    fun `when onAdditionalDetails is set in checkout callbacks, onAdditionalDetails in sessions component triggers the set callback`() =
        runTest {
            val checkoutCallbacks = CheckoutCallbacks(
                onSubmit = onSubmitCallback,
                onAdditionalDetails = onAdditionalDetailsCallback,
                onError = onErrorCallback,
            )

            whenever(onAdditionalDetailsCallback.onAdditionalDetails(any())) doReturn CheckoutResult.Finished()

            val actualSessionsComponentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks()

            actualSessionsComponentCallbacks.onAdditionalDetails(TEST_ACTION_COMPONENT_DATA)
            verify(checkoutCallbacks.onAdditionalDetails)?.onAdditionalDetails(TEST_ACTION_COMPONENT_DATA)
        }

    @Test
    fun `when onError is set in checkout callbacks, onError in sessions component triggers it`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
        )

        val actualSessionsComponentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks()

        actualSessionsComponentCallbacks.onError(TEST_CHECKOUT_ERROR)
        verify(checkoutCallbacks.onError)?.onError(TEST_CHECKOUT_ERROR)
    }

    @Test
    fun `when onFinished is set in checkout callbacks, onFinished in sessions component triggers it`() = runTest {
        val checkoutCallbacks = CheckoutCallbacks(
            onSubmit = onSubmitCallback,
            onAdditionalDetails = onAdditionalDetailsCallback,
            onError = onErrorCallback,
            onFinished = onFinishedCallback,
        )

        val actualSessionsComponentCallbacks = checkoutCallbacks.toSessionsComponentCallbacks()

        actualSessionsComponentCallbacks.onFinished(TEST_PAYMENT_RESULT)
        verify(checkoutCallbacks.onFinished)?.onFinished(TEST_PAYMENT_RESULT)
    }

    companion object {
        private val TEST_PAYMENT_COMPONENT_STATE = TestPaymentComponentState()
        private val TEST_ACTION_COMPONENT_DATA = ActionComponentData()
        private val TEST_CHECKOUT_ERROR = CheckoutError(
            code = CheckoutError.ErrorCode.UNKNOWN,
            message = "Test error",
        )
        private val TEST_PAYMENT_RESULT = PaymentResult(
            resultCode = "authorised",
            sessionId = null,
            sessionResult = null,
            sessionData = null,
            order = null,
        )
    }
}
