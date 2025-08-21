package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.action.internal.ActionComponentEvent
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.ComponentError
import com.adyen.checkout.core.components.OnAdditionalDetailsCallback
import com.adyen.checkout.core.components.OnSubmitCallback
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.internal.SessionsComponentCallbacks
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.components.paymentmethod.TestPaymentComponentState
import com.adyen.checkout.core.sessions.SessionPaymentResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class SessionsComponentEventHandlerTest(
    @Mock private val sessionInteractor: SessionInteractor,
    @Mock private val componentCallbacks: SessionsComponentCallbacks,
) {

    private lateinit var sessionsComponentEventHandler: SessionsComponentEventHandler<TestPaymentComponentState>

    @BeforeEach
    fun beforeEach() {
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = componentCallbacks,
        )
    }

    @Test
    fun `when event is submit and callback is null, then session interactor is used`() = runTest {
        whenever(sessionInteractor.submitPayment(any())) doReturn SessionCallResult.Payments.Finished(mock())
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = componentCallbacks,
        )

        val state = TestPaymentComponentState()
        sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        verify(sessionInteractor).submitPayment(state)
    }

    @Test
    fun `when event is submit and onSubmit is overridden and returns a result, then that result is returned`() = runTest {
        val expectedResult = CheckoutResult.Finished()
        whenever(componentCallbacks.onSubmit) doReturn object : OnSubmitCallback {
            override suspend fun onSubmit(paymentComponentState: PaymentComponentState<*>): CheckoutResult {
                return expectedResult
            }
        }
        whenever(componentCallbacks.onSubmit(any())) doReturn expectedResult

        val state = TestPaymentComponentState()
        val result = sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        assertEquals(expectedResult, result)
        verify(componentCallbacks).onSubmit(state)
        verify(sessionInteractor, never()).submitPayment(any())
    }

    @ParameterizedTest
    @MethodSource("sessionInteractorSubmitSource")
    fun `when session interactor submits payment, then the correct checkout result is returned`(
        sessionResult: SessionCallResult.Payments,
        expectedResult: CheckoutResult,
    ) = runTest {
        whenever(componentCallbacks.beforeSubmit(any())) doReturn false
        whenever(sessionInteractor.submitPayment(any())) doReturn sessionResult

        val state = TestPaymentComponentState()
        val result = sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        if (result is CheckoutResult.Error && expectedResult is CheckoutResult.Error) {
            val resultException = result.componentError.exception
            val expectedException = expectedResult.componentError.exception
            // We can't compare the RuntimeException instances directly, so we compare their cause.
            assertEquals(expectedException.cause, resultException.cause)
        } else {
            assertEquals(expectedResult, result)
        }
    }

    @Test
    fun `when event is action details and callback is null, then session interactor is used`() = runTest {
        whenever(sessionInteractor.submitDetails(any())) doReturn SessionCallResult.Details.Finished(mock())
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = componentCallbacks,
        )

        val data = ActionComponentData(paymentData = "test")
        sessionsComponentEventHandler.onActionComponentEvent(ActionComponentEvent.ActionDetails(data))

        verify(sessionInteractor).submitDetails(data)
    }

    @Test
    fun `when event is action details and onAdditionalDetails is overridden and returns a result, then that result is returned`() =
        runTest {
            val expectedResult = CheckoutResult.Finished()
            whenever(componentCallbacks.onAdditionalDetails) doReturn object : OnAdditionalDetailsCallback {
                override suspend fun onAdditionalDetails(actionComponentData: ActionComponentData): CheckoutResult {
                    return expectedResult
                }
            }
            whenever(componentCallbacks.onAdditionalDetails(any())) doReturn expectedResult

            val data = ActionComponentData(paymentData = "test")
            val result = sessionsComponentEventHandler.onActionComponentEvent(ActionComponentEvent.ActionDetails(data))

            assertEquals(expectedResult, result)
            verify(componentCallbacks).onAdditionalDetails(data)
            verify(sessionInteractor, never()).submitDetails(any())
        }

    @ParameterizedTest
    @MethodSource("sessionInteractorDetailsSource")
    fun `when session interactor submits details, then the correct checkout result is returned`(
        sessionResult: SessionCallResult.Details,
        expectedResult: CheckoutResult,
    ) = runTest {
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            componentCallbacks = componentCallbacks,
        )
        whenever(sessionInteractor.submitDetails(any())) doReturn sessionResult

        val data = ActionComponentData(paymentData = "test")
        val result = sessionsComponentEventHandler.onActionComponentEvent(ActionComponentEvent.ActionDetails(data))

        if (result is CheckoutResult.Error && expectedResult is CheckoutResult.Error) {
            val resultException = result.componentError.exception
            val expectedException = expectedResult.componentError.exception
            // We can't compare the RuntimeException instances directly, so we compare their cause.
            assertEquals(expectedException.cause, resultException.cause)
        } else {
            assertEquals(expectedResult, result)
        }
    }

    @Test
    fun `when session interactor submit details returns error, then onError is called and error result is returned`() =
        runTest {
            val componentError = ComponentError(RuntimeException("test_error"))
            val event = ActionComponentEvent.Error(componentError)

            val result = sessionsComponentEventHandler.onActionComponentEvent(event)

            verify(componentCallbacks).onError(componentError)
            assertEquals(CheckoutResult.Error(componentError), result)
        }

    companion object {

        private val throwable = RuntimeException("test_error")

        @JvmStatic
        fun sessionInteractorSubmitSource() = listOf(
            // sessionResult, checkoutResult
            arguments(SessionCallResult.Payments.Action(TestAction()), CheckoutResult.Action(TestAction())),
            arguments(
                SessionCallResult.Payments.Error(throwable),
                CheckoutResult.Error(ComponentError(RuntimeException(throwable))),
            ),
            arguments(
                SessionCallResult.Payments.Finished(SessionPaymentResult(null, null, null, null, null)),
                CheckoutResult.Finished(),
            ),
        )

        @JvmStatic
        fun sessionInteractorDetailsSource() = listOf(
            // sessionResult, checkoutResult
            arguments(SessionCallResult.Details.Action(TestAction()), CheckoutResult.Action(TestAction())),
            arguments(
                SessionCallResult.Details.Error(throwable),
                CheckoutResult.Error(ComponentError(RuntimeException(throwable))),
            ),
            arguments(
                SessionCallResult.Details.Finished(SessionPaymentResult(null, null, null, null, null)),
                CheckoutResult.Finished(),
            ),
        )
    }
}
