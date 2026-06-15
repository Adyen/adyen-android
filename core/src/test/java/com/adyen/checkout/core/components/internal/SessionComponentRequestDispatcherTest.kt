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
import com.adyen.checkout.core.common.CheckoutResultCode
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.BeforeSubmitResult
import com.adyen.checkout.core.components.SessionCheckoutCallbacks
import com.adyen.checkout.core.components.SessionCheckoutResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.Address
import com.adyen.checkout.core.components.data.BeforeSubmitData
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.ShopperName
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
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
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
        fun `when submit succeeds with action, then onComplete is not invoked`() = runTest {
            val response = createPaymentsResponse(action = mock())
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            var onCompleteCalls = 0
            val dispatcher = createDispatcher(onComplete = { onCompleteCalls++ })

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onCompleteCalls)
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
        fun `when submit succeeds without result code, then Completion is returned with unknown result code`() =
            runTest {
                val response = createPaymentsResponse(resultCode = null)
                whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

                val dispatcher = createDispatcher()

                val result = dispatcher.submit(emptyPaymentComponentData())

                assertEquals(SubmitResult.Completion("Unknown"), result)
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
        fun `when submit fails, then onComplete is not invoked`() = runTest {
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.failure(IOException())

            var onCompleteCalls = 0
            val dispatcher = createDispatcher(onComplete = { onCompleteCalls++ })

            dispatcher.submit(emptyPaymentComponentData())

            assertEquals(0, onCompleteCalls)
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
        fun `when additionalDetails succeeds without result code, then Completion is returned with unknown result code`() =
            runTest {
                val response = createDetailsResponse(resultCode = null)
                whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.success(response)

                val dispatcher = createDispatcher()

                val result = dispatcher.additionalDetails(ActionComponentData())

                assertEquals(AdditionalDetailsResult.Completion("Unknown"), result)
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
        fun `when additionalDetails fails, then onComplete is not invoked`() = runTest {
            whenever(sessionRepository.submitDetails(any(), any(), any())) doReturn Result.failure(IOException())

            var onCompleteCalls = 0
            val dispatcher = createDispatcher(onComplete = { onCompleteCalls++ })

            dispatcher.additionalDetails(ActionComponentData())

            assertEquals(0, onCompleteCalls)
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

    @Nested
    inner class CompleteTest {

        @Test
        fun `when complete is called, then onComplete callback is invoked with AdvancedCheckoutResult`() {
            val capturedResults = mutableListOf<SessionCheckoutResult>()
            val dispatcher = createDispatcher(onComplete = { capturedResults += it })

            dispatcher.complete(CheckoutResultCode.AUTHORISED)

            val expected = SessionCheckoutResult(
                CheckoutResultCode.AUTHORISED,
                sessionId = "session-id",
                sessionData = "session-data",
            )
            assertEquals(listOf(expected), capturedResults)
        }

        @Test
        fun `when complete is called with a custom result code, then the result code is wrapped correctly`() {
            val capturedResults = mutableListOf<SessionCheckoutResult>()
            val dispatcher = createDispatcher(onComplete = { capturedResults += it })

            dispatcher.complete(CheckoutResultCode("CustomResultCode"))

            assertEquals(CheckoutResultCode("CustomResultCode"), capturedResults.single().resultCode)
        }
    }

    @Nested
    inner class OnBeforeSubmitTest {

        @Test
        fun `when onBeforeSubmit is null then submission proceeds directly`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val dispatcher = createDispatcher(onBeforeSubmit = null)

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Completion("Authorised"), result)
        }

        @Test
        fun `when onBeforeSubmit returns Proceed with modified data then data is applied`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val modifiedName = ShopperName(firstName = "Modified", lastName = "Name")
            val dispatcher = createDispatcher(
                onBeforeSubmit = { data ->
                    BeforeSubmitResult.Proceed(
                        data = data.copy(shopperName = modifiedName, shopperEmail = "modified@test.com"),
                    )
                },
            )

            val inputData = emptyPaymentComponentData()
            dispatcher.submit(inputData)

            val capturedData = argumentCaptor<PaymentComponentData<*>>()
            verify(sessionRepository).submitPayment(any(), any(), capturedData.capture())
            assertEquals(modifiedName, capturedData.firstValue.shopperName)
            assertEquals("modified@test.com", capturedData.firstValue.shopperEmail)
        }

        @Test
        fun `when onBeforeSubmit returns Proceed with sessionData then session state is updated`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val dispatcher = createDispatcher(
                onBeforeSubmit = { data ->
                    BeforeSubmitResult.Proceed(
                        data = data,
                        sessionData = "patched-session-data",
                    )
                },
            )

            dispatcher.submit(emptyPaymentComponentData())

            val capturedSessionData = argumentCaptor<String>()
            verify(sessionRepository).submitPayment(any(), capturedSessionData.capture(), any())
            assertEquals("patched-session-data", capturedSessionData.firstValue)
        }

        @Test
        fun `when onBeforeSubmit returns Abort then submission stops and onError is not called`() = runTest {
            var onErrorCalls = 0
            val dispatcher = createDispatcher(
                onFailure = { onErrorCalls++ },
                onBeforeSubmit = { BeforeSubmitResult.Abort() },
            )

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Retry(), result)
            assertEquals(0, onErrorCalls)
            verify(sessionRepository, never()).submitPayment(any(), any(), any())
        }

        @Test
        fun `when applyBeforeSubmitData has null fields then original values are preserved`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val originalName = ShopperName(firstName = "Original", lastName = "Name")
            val originalEmail = "original@test.com"
            val dispatcher = createDispatcher(
                onBeforeSubmit = { data ->
                    // Return data with null shopperName — should keep original
                    BeforeSubmitResult.Proceed(
                        data = data.copy(shopperName = null),
                    )
                },
            )

            val inputData = PaymentComponentData<PaymentMethodDetails>(
                paymentMethod = null,
                order = null,
                shopperName = originalName,
                shopperEmail = originalEmail,
            )
            dispatcher.submit(inputData)

            val capturedData = argumentCaptor<PaymentComponentData<*>>()
            verify(sessionRepository).submitPayment(any(), any(), capturedData.capture())
            assertEquals(originalName, capturedData.firstValue.shopperName)
            assertEquals(originalEmail, capturedData.firstValue.shopperEmail)
        }

        @Test
        fun `when applyBeforeSubmitData has non-null fields then they override originals`() = runTest {
            val response = createPaymentsResponse(resultCode = "Authorised")
            whenever(sessionRepository.submitPayment(any(), any(), any())) doReturn Result.success(response)

            val newAddress = Address()
            val dispatcher = createDispatcher(
                onBeforeSubmit = { data ->
                    BeforeSubmitResult.Proceed(
                        data = data.copy(billingAddress = newAddress),
                    )
                },
            )

            val inputData = PaymentComponentData<PaymentMethodDetails>(
                paymentMethod = null,
                order = null,
                billingAddress = null,
            )
            dispatcher.submit(inputData)

            val capturedData = argumentCaptor<PaymentComponentData<*>>()
            verify(sessionRepository).submitPayment(any(), any(), capturedData.capture())
            assertEquals(newAddress, capturedData.firstValue.billingAddress)
        }

        @Test
        fun `when onBeforeSubmit throws exception then onError is called and submission stops`() = runTest {
            var onErrorCalls = 0
            val capturedErrors = mutableListOf<CheckoutError>()
            val dispatcher = createDispatcher(
                onFailure = { error ->
                    onErrorCalls++
                    capturedErrors.add(error)
                },
                onBeforeSubmit = {
                    throw IllegalStateException("Merchant callback error")
                },
            )

            val result = dispatcher.submit(emptyPaymentComponentData())

            assertEquals(SubmitResult.Retry(), result)
            assertEquals(1, onErrorCalls)
            assertEquals(1, capturedErrors.size)
            verify(sessionRepository, never()).submitPayment(any(), any(), any())
        }
    }

    private fun createDispatcher(
        onComplete: (SessionCheckoutResult) -> Unit = {},
        onFailure: (CheckoutError) -> Unit = {},
        onBeforeSubmit: (suspend (BeforeSubmitData) -> BeforeSubmitResult)? = null,
    ): SessionComponentRequestDispatcher = SessionComponentRequestDispatcher(
        initialSessionData = "session-data",
        sessionId = "session-id",
        callbacks = SessionCheckoutCallbacks(
            onComplete = onComplete,
            onFailure = onFailure,
            onBeforeSubmit = onBeforeSubmit,
        ),
        sessionRepository = sessionRepository,
    )

    private fun createPaymentsResponse(
        resultCode: String? = "",
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
        resultCode: String?,
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
