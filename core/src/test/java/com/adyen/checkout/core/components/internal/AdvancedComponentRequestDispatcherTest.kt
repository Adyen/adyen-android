/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.AdvancedCheckoutCallbacks
import com.adyen.checkout.core.components.AdvancedCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.error.CheckoutError
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class AdvancedComponentRequestDispatcherTest {

    @Nested
    inner class SubmitTest {

        @Test
        fun `when onSubmit returns Completion, then Completion is returned`() = runTest {
            val dispatcher = createDispatcher(onSubmit = { SubmitResult.Completion("Authorised") })

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Completion("Authorised"), result)
        }

        @Test
        fun `when onSubmit returns Completion, then onComplete is invoked with correct result`() = runTest {
            val capturedResults = mutableListOf<AdvancedCheckoutResult>()
            val dispatcher = createDispatcher(
                onSubmit = { SubmitResult.Completion("Authorised") },
                onComplete = { capturedResults += it },
            )

            dispatcher.submit(emptyPaymentComponentData())

            val expected = AdvancedCheckoutResult(resultCode = CheckoutResultCode("Authorised"))
            assertEquals(expected, capturedResults.single())
        }

        @Test
        fun `when onSubmit returns Action, then Action is returned`() = runTest {
            val action = mock<Action>()
            val dispatcher = createDispatcher(onSubmit = { SubmitResult.Action(action) })

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Action(action), result)
        }

        @Test
        fun `when onSubmit returns Action, then onComplete is not invoked`() = runTest {
            var onCompleteCalls = 0
            val dispatcher = createDispatcher(
                onSubmit = { SubmitResult.Action(mock()) },
                onComplete = { onCompleteCalls++ },
            )

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onCompleteCalls)
        }

        @Test
        fun `when onSubmit returns Retry, then Retry is returned`() = runTest {
            val dispatcher = createDispatcher(onSubmit = { SubmitResult.Retry("try again") })

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Retry("try again"), result)
        }

        @Test
        fun `when onSubmit returns Retry, then onComplete is not invoked`() = runTest {
            var onCompleteCalls = 0
            val dispatcher = createDispatcher(
                onSubmit = { SubmitResult.Retry() },
                onComplete = { onCompleteCalls++ },
            )

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onCompleteCalls)
        }
    }

    @Nested
    inner class AdditionalDetailsTest {

        @Test
        fun `when onAdditionalDetails returns Completion, then Completion is returned`() = runTest {
            val dispatcher = createDispatcher(
                onAdditionalDetails = { AdditionalDetailsResult.Completion("Authorised") },
            )

            val result = dispatcher.additionalDetails(ActionComponentData())

            assertEquals(AdditionalDetailsResult.Completion("Authorised"), result)
        }

        @Test
        fun `when onAdditionalDetails returns Completion, then onComplete is invoked with correct result`() =
            runTest {
                val capturedResults = mutableListOf<AdvancedCheckoutResult>()
                val dispatcher = createDispatcher(
                    onAdditionalDetails = { AdditionalDetailsResult.Completion("Authorised") },
                    onComplete = { capturedResults += it },
                )

                dispatcher.additionalDetails(ActionComponentData())

                val expected = AdvancedCheckoutResult(resultCode = CheckoutResultCode("Authorised"))
                assertEquals(expected, capturedResults.single())
            }
    }

    @Nested
    inner class ErrorTest {

        @Test
        fun `when error is called, then onError is invoked`() {
            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onFailure = { capturedErrors += it })

            val error = CheckoutError(code = "test", message = "test error")
            dispatcher.failure(error)

            assertEquals(listOf(error), capturedErrors)
        }
    }

    private fun createDispatcher(
        onSubmit: suspend (PaymentComponentData<*>) -> SubmitResult = { SubmitResult.Completion("") },
        onAdditionalDetails: suspend (ActionComponentData) -> AdditionalDetailsResult = {
            AdditionalDetailsResult.Completion("")
        },
        onFailure: (CheckoutError) -> Unit = {},
        onComplete: (AdvancedCheckoutResult) -> Unit = {},
    ): AdvancedComponentRequestDispatcher = AdvancedComponentRequestDispatcher(
        callbacks = AdvancedCheckoutCallbacks(
            onSubmit = onSubmit,
            onAdditionalDetails = onAdditionalDetails,
            onFailure = onFailure,
            onComplete = onComplete,
        ),
    )

    private fun emptyPaymentComponentData(): PaymentComponentData<PaymentMethodDetails> =
        PaymentComponentData(paymentMethod = null, order = null)
}
