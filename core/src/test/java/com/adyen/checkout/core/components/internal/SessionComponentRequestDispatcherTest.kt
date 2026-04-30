/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

/**
 * Verifies session-internal API failure routing in [SessionComponentRequestDispatcher].
 *
 * Both `submit()` and `additionalDetails()` must, on `SessionRepository` failure, route the
 * original cause to `callbacks.onError(...)` and then abort the launched coroutine via
 * `CancellationException` so the orchestrator does not invoke `handleResult`. This mirrors iOS
 * `Session.didSubmit` / `Session.didProvide` behavior. See the
 * `SUBMIT_AND_ADDITIONAL_DETAILS_RESULT_DECISION` plan for the full rationale.
 */
@ExtendWith(MockitoExtension::class)
internal class SessionComponentRequestDispatcherTest(
    @Mock private val sessionRepository: SessionRepository,
) {

    @Test
    fun `when submitPayment fails, then onError is invoked with the mapped error and CancellationException is thrown`() =
        runTest {
            val cause = IOException("network down")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(cause)

            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onError = { capturedErrors += it })

            val thrown = assertThrows<CancellationException> {
                dispatcher.submit(emptyPaymentComponentData())
            }

            assertEquals(1, capturedErrors.size)
            assertSame(cause, capturedErrors.single().cause)
            assertSame(cause, thrown.cause)
        }

    @Test
    fun `when submitDetails fails, then onError is invoked with the mapped error and CancellationException is thrown`() =
        runTest {
            val cause = IOException("network down")
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(cause)

            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(onError = { capturedErrors += it })

            val thrown = assertThrows<CancellationException> {
                dispatcher.additionalDetails(ActionComponentData())
            }

            assertEquals(1, capturedErrors.size)
            assertSame(cause, capturedErrors.single().cause)
            assertSame(cause, thrown.cause)
        }

    @Test
    fun `when submitPayment fails, then onFinished is not invoked`() = runTest {
        whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(IOException())

        var onFinishedCalls = 0
        val dispatcher = createDispatcher(onFinished = { onFinishedCalls++ })

        assertThrows<CancellationException> {
            dispatcher.submit(emptyPaymentComponentData())
        }

        assertEquals(0, onFinishedCalls)
    }

    @Test
    fun `when submitDetails fails, then onFinished is not invoked`() = runTest {
        whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(IOException())

        var onFinishedCalls = 0
        val dispatcher = createDispatcher(onFinished = { onFinishedCalls++ })

        assertThrows<CancellationException> {
            dispatcher.additionalDetails(ActionComponentData())
        }

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
