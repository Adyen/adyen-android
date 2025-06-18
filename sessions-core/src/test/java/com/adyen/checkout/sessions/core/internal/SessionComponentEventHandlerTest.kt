/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/2/2023.
 */

package com.adyen.checkout.sessions.core.internal

import android.os.Parcel
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.BaseComponentCallback
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.old.PermissionHandlerCallback
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.sessions.core.SessionComponentCallback
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.sessions.core.TestComponentState
import com.adyen.checkout.test.LoggingExtension
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, LoggingExtension::class)
internal class SessionComponentEventHandlerTest(
    @Mock private val sessionInteractor: SessionInteractor,
    @Mock private val sessionSavedStateHandleContainer: SessionSavedStateHandleContainer,
) {

    private lateinit var sessionComponentEventHandler: SessionComponentEventHandler<PaymentComponentState<*>>

    @BeforeEach
    fun beforeEach() {
        sessionComponentEventHandler = SessionComponentEventHandler(sessionInteractor, sessionSavedStateHandleContainer)
    }

    @Test
    fun `when session is changed, then session data should be updated`() {
        val sessionFlow = MutableStateFlow(SessionModel("id", "sessionData"))
        whenever(sessionInteractor.sessionFlow) doReturn sessionFlow
        sessionComponentEventHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()))

        sessionFlow.tryEmit(SessionModel("id", "updatedSessionData"))

        verify(sessionSavedStateHandleContainer).updateSessionData("updatedSessionData")
    }

    @Nested
    @DisplayName("when payment component event")
    inner class OnPaymentComponentEventTest {

        @BeforeEach
        fun beforeEach() {
            whenever(sessionInteractor.sessionFlow) doReturn flowOf()
            sessionComponentEventHandler.initialize(CoroutineScope(UnconfinedTestDispatcher()))
        }

        @Test
        fun `and component callback is wrongly typed, then an error should be thrown`() {
            assertThrows<CheckoutException> {
                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    object : BaseComponentCallback {},
                )
            }
        }

        @Nested
        @DisplayName("is Submit")
        inner class SubmitTest {

            @Test
            fun `then loading state should be propagated properly`() = runTest {
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.Action(createTestAction())
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(callback).onLoading(true)
                verify(callback).onLoading(false)
            }

            @Test
            fun `and result is Action, then action should be propagated`() = runTest {
                val action = createTestAction()
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.Action(action)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(callback).onAction(action)
            }

            @Test
            fun `and result is Error, then error should be propagated`() = runTest {
                val error = RuntimeException("Test")
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.Error(error)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                val errorCaptor = argumentCaptor<ComponentError>()
                verify(callback).onError(errorCaptor.capture())
                assertEquals(error, errorCaptor.lastValue.exception.cause)
            }

            @Test
            fun `and result is Finished, then result should be propagated`() = runTest {
                val result = createSessionPaymentResult()
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.Finished(result)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(callback).onFinished(result)
            }

            @Test
            fun `and result is NotFullyPaidOrder, then result should be propagated`() = runTest {
                val result = createSessionPaymentResult()
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.NotFullyPaidOrder(result)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(callback).onFinished(result)
            }

            @Test
            fun `and result is RefusedPartialPayment, then result should be propagated`() = runTest {
                val result = createSessionPaymentResult()
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.RefusedPartialPayment(result)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(callback).onFinished(result)
            }

            @Test
            fun `and result is TakenOver, then this should be set`() = runTest {
                whenever(sessionInteractor.onPaymentsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Payments.TakenOver
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Submit(createPaymentComponentState()),
                    callback,
                )

                verify(sessionSavedStateHandleContainer).isFlowTakenOver = true
            }
        }

        @Nested
        @DisplayName("is ActionDetails")
        inner class ActionDetailsTest {

            @Test
            fun `then loading state should be propagated properly`() = runTest {
                whenever(sessionInteractor.onDetailsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Details.Action(createTestAction())
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.ActionDetails(ActionComponentData()),
                    callback,
                )

                verify(callback).onLoading(true)
                verify(callback).onLoading(false)
            }

            @Test
            fun `and result is Action, then action should be propagated`() = runTest {
                val action = createTestAction()
                whenever(sessionInteractor.onDetailsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Details.Action(action)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.ActionDetails(ActionComponentData()),
                    callback,
                )

                verify(callback).onAction(action)
            }

            @Test
            fun `and result is Error, then error should be propagated`() = runTest {
                val error = RuntimeException("Test")
                whenever(sessionInteractor.onDetailsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Details.Error(error)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.ActionDetails(ActionComponentData()),
                    callback,
                )

                val errorCaptor = argumentCaptor<ComponentError>()
                verify(callback).onError(errorCaptor.capture())
                assertEquals(error, errorCaptor.lastValue.exception.cause)
            }

            @Test
            fun `and result is Finished, then result should be propagated`() = runTest {
                val result = createSessionPaymentResult()
                whenever(sessionInteractor.onDetailsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Details.Finished(result)
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.ActionDetails(ActionComponentData()),
                    callback,
                )

                verify(callback).onFinished(result)
            }

            @Test
            fun `and result is TakenOver, then this should be set`() = runTest {
                whenever(sessionInteractor.onDetailsCallRequested(any(), any(), any())) doReturn
                    SessionCallResult.Details.TakenOver
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.ActionDetails(ActionComponentData()),
                    callback,
                )

                verify(sessionSavedStateHandleContainer).isFlowTakenOver = true
            }
        }

        @Nested
        @DisplayName("is StateChanged")
        inner class StateChangedTest {

            @Test
            fun `then state change should be propagated`() = runTest {
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()
                val componentState = createPaymentComponentState()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.StateChanged(componentState),
                    callback,
                )

                verify(callback).onStateChanged(componentState)
            }
        }

        @Nested
        @DisplayName("is Error")
        inner class ErrorTest {

            @Test
            fun `then error be propagated`() = runTest {
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()
                val error = ComponentError(CheckoutException("Test"))

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.Error(error),
                    callback,
                )

                verify(callback).onError(error)
            }
        }

        @Nested
        @DisplayName("is PermissionRequested")
        inner class PermissionRequestedTest {

            @Test
            fun `then permission requested should be propagated`() = runTest {
                val callback = mock<SessionComponentCallback<PaymentComponentState<*>>>()
                val requiredPermission = "Required Permission"
                val permissionCallback = mock<PermissionHandlerCallback>()

                sessionComponentEventHandler.onPaymentComponentEvent(
                    PaymentComponentEvent.PermissionRequest(requiredPermission, permissionCallback),
                    callback,
                )

                verify(callback).onPermissionRequest(requiredPermission, permissionCallback)
            }
        }
    }

    private fun createPaymentComponentState() = TestComponentState(
        data = PaymentComponentData(null, null, null),
        isInputValid = false,
        isReady = false,
    )

    private fun createTestAction(
        type: String = "test",
        paymentData: String = "paymentData",
        paymentMethodType: String = "paymentMethodType",
    ) = object : Action() {
        override var type: String? = type
        override var paymentData: String? = paymentData
        override var paymentMethodType: String? = paymentMethodType
        override fun writeToParcel(dest: Parcel, flags: Int) = Unit
    }

    private fun createSessionPaymentResult() = SessionPaymentResult(
        sessionId = "sessionId",
        sessionResult = "sessionResult",
        sessionData = "sessionData",
        resultCode = "resultCode",
        order = null,
    )
}
