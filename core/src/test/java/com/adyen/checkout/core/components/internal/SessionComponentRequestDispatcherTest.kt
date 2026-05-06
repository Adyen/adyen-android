/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.IOException

@ExtendWith(MockitoExtension::class)
internal class SessionComponentRequestDispatcherTest(
    @Mock private val sessionRepository: SessionRepository,
) {

    @Test
    fun `when submitPayment fails, then retry is returned and onError is invoked`() =
        runTest {
            val cause = IOException("network down")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(cause)

            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onError = { capturedErrors += it })

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Retry("network down"), result)
            assertEquals(1, capturedErrors.size)
        }

    @Test
    fun `when submitDetails fails, then error completion is returned and onError is invoked`() =
        runTest {
            val cause = IOException("network down")
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(cause)

            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onError = { capturedErrors += it })

            val result = dispatcher.additionalDetails(ActionComponentData())

            assertEquals(AdditionalDetailsResult.Completion("Error"), result)
            assertEquals(1, capturedErrors.size)
        }

    @Test
    fun `when submitPayment fails, then onFinished is not invoked`() = runTest {
        whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(IOException())

        var onFinishedCalls = 0
        val dispatcher = createDispatcher(onFinished = { onFinishedCalls++ })

        dispatcher.submit(emptyPaymentComponentData())

        assertEquals(0, onFinishedCalls)
    }

    @Test
    fun `when submitDetails fails, then onFinished is not invoked`() = runTest {
        whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(IOException())

        var onFinishedCalls = 0
        val dispatcher = createDispatcher(onFinished = { onFinishedCalls++ })

        dispatcher.additionalDetails(ActionComponentData())

        assertEquals(0, onFinishedCalls)
    }

    private fun createDispatcher(
        onFinished: () -> Unit = {},
        onError: (CheckoutError) -> Unit = {},
    ): SessionComponentRequestDispatcher = SessionComponentRequestDispatcher(
        initialSessionData = "session-data",
        sessionId = "session-id",
        callbacks = SessionCheckoutCallbacks(
            onFinished = onFinished,
            onError = onError,
        ),
        sessionRepository = sessionRepository,
    )

    private fun emptyPaymentComponentData(): PaymentComponentData<PaymentMethodDetails> =
        PaymentComponentData(paymentMethod = null, order = null)
}
