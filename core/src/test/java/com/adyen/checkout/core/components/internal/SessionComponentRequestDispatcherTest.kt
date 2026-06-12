/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionPaymentsResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.IOException

@ExtendWith(MockitoExtension::class)
internal class SessionComponentRequestDispatcherTest(
    @param:Mock private val sessionRepository: SessionRepository,
) {

    @Nested
    inner class SubmitTest {

        @Test
        fun `when submit succeeds with action, then Action is returned`() = runTest {
            val action = mock<Action>()
            val response = createPaymentsResponse(action = action)
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val dispatcher = createDispatcher()

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Action(action), result)
        }

        @Test
        fun `when submit succeeds with action, then onFinished is not invoked`() = runTest {
            val response = createPaymentsResponse(action = mock())
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            var onFinishedCalls = 0
            val dispatcher = createDispatcher(onComplete = { onFinishedCalls++ })

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onFinishedCalls)
        }

        @Test
        fun `when submit succeeds without action, then Completion is returned`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val dispatcher = createDispatcher()

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Completion("Authorised"), result)
        }

        @Test
        fun `when submit succeeds without action, then onFinished is invoked`() = runTest {
            val response = createPaymentsResponse()
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            var onFinishedCalls = 0
            val dispatcher = createDispatcher(onComplete = { onFinishedCalls++ })

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(1, onFinishedCalls)
        }

        @Test
        fun `when submit fails, then retry is returned and onFailure is invoked`() =
            runTest {
                val cause = IOException("network down")
                whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(cause)

                val capturedErrors = mutableListOf<CheckoutError>()
                val dispatcher = createDispatcher(onFailure = { capturedErrors += it })

                val result = dispatcher.submit(emptyPaymentComponentData())

                assertEquals(SubmitResult.Retry("network down"), result)
                assertEquals(1, capturedErrors.size)
            }

        @Test
        fun `when submit fails, then onFinished is not invoked`() = runTest {
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(IOException())

            var onFinishedCalls = 0
            val dispatcher = createDispatcher(onComplete = { onFinishedCalls++ })

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onFinishedCalls)
        }
    }

    @Nested
    inner class AdditionalDetailsTest {

        @Test
        fun `when additionalDetails succeeds, then Completion is returned`() = runTest {
            val response = createDetailsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.success(response)

            val dispatcher = createDispatcher()

            val result = dispatcher.additionalDetails(ActionComponentData())

            assertEquals(AdditionalDetailsResult.Completion("Authorised"), result)
        }

        @Test
        fun `when additionalDetails succeeds, then onFinished is invoked`() = runTest {
            val response = createDetailsResponse()
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.success(response)

            var onFinishedCalls = 0
            val dispatcher = createDispatcher(onComplete = { onFinishedCalls++ })

            dispatcher.additionalDetails(ActionComponentData())

            assertEquals(1, onFinishedCalls)
        }

        @Test
        fun `when additionalDetails fails, then error completion is returned and onFailure is invoked`() =
            runTest {
                val cause = IOException("network down")
                whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(cause)

                val capturedErrors = mutableListOf<CheckoutError>()
                val dispatcher = createDispatcher(onFailure = { capturedErrors += it })

                val result = dispatcher.additionalDetails(ActionComponentData())

                assertEquals(AdditionalDetailsResult.Completion("Error"), result)
                assertEquals(1, capturedErrors.size)
            }

        @Test
        fun `when additionalDetails fails, then onFinished is not invoked`() = runTest {
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(IOException())

            var onFinishedCalls = 0
            val dispatcher = createDispatcher(onComplete = { onFinishedCalls++ })

            dispatcher.additionalDetails(ActionComponentData())

            assertEquals(0, onFinishedCalls)
        }
    }

    @Nested
    inner class ErrorTest {

        @Test
        fun `when error is called, then onFailure is invoked`() {
            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onFailure = { capturedErrors += it })

            val error = CheckoutError(code = "test", message = "test error")
            dispatcher.failure(error)

            assertEquals(listOf(error), capturedErrors)
        }
    }

    private fun createDispatcher(
        onComplete: () -> Unit = {},
        onFailure: (CheckoutError) -> Unit = {},
    ): SessionComponentRequestDispatcher = SessionComponentRequestDispatcher(
        initialSessionData = "session-data",
        sessionId = "session-id",
        callbacks = SessionCheckoutCallbacks(
            onComplete = onComplete,
            onFailure = onFailure,
        ),
        sessionRepository = sessionRepository,
    )

    private fun createPaymentsResponse(
        resultCode: String? = null,
        action: Action? = null,
    ) = SessionPaymentsResponse(
        sessionData = "session-data",
        status = null,
        resultCode = resultCode,
        action = action,
        order = null,
        sessionResult = null,
    )

    private fun createDetailsResponse(
        resultCode: String? = null,
    ) = SessionDetailsResponse(
        sessionData = "session-data",
        status = null,
        resultCode = resultCode,
        action = null,
        sessionResult = null,
        order = null,
    )

    private fun emptyPaymentComponentData(): PaymentComponentData<PaymentMethodDetails> =
        PaymentComponentData(paymentMethod = null, order = null)
}
