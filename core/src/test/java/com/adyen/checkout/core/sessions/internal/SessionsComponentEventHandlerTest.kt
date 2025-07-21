package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.action.data.TestAction
import com.adyen.checkout.core.components.CheckoutCallbacks
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import com.adyen.checkout.core.components.paymentmethod.TestComponentState
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
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class)
internal class SessionsComponentEventHandlerTest(
    @Mock private val sessionInteractor: SessionInteractor,
    @Mock private val checkoutCallback: CheckoutCallbacks,
) {

    private lateinit var sessionsComponentEventHandler: SessionsComponentEventHandler<TestComponentState>

    @BeforeEach
    fun beforeEach() {
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            checkoutCallbacks = checkoutCallback,
        )
    }

    @Test
    fun `when event is submit and callback is null, then session interactor is used`() = runTest {
        whenever(sessionInteractor.submitPayment(any())) doReturn SessionCallResult.Payments.Finished(mock())
        sessionsComponentEventHandler = SessionsComponentEventHandler(
            sessionInteractor = sessionInteractor,
            checkoutCallbacks = null,
        )

        val state = TestComponentState()
        sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        verify(sessionInteractor).submitPayment(state)
    }

    @Test
    fun `when event is submit and beforeSubmit returns false, then session interactor is used`() = runTest {
        whenever(checkoutCallback.beforeSubmit(any())) doReturn false
        whenever(sessionInteractor.submitPayment(any())) doReturn SessionCallResult.Payments.Finished(mock())

        val state = TestComponentState()
        sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        verify(sessionInteractor).submitPayment(state)
    }

    @Test
    fun `when event is submit and beforeSubmit returns true, then onSubmit is called`() = runTest {
        whenever(checkoutCallback.beforeSubmit(any())) doReturn true
        whenever(sessionInteractor.submitPayment(any())) doReturn SessionCallResult.Payments.Finished(mock())

        val state = TestComponentState()
        sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        checkoutCallback.onSubmit(state)
    }

    @ParameterizedTest
    @MethodSource("sessionInteractorSubmitSource")
    fun `when session interactor submits payment and the result is action, then onSubmit is called`(
        sessionResult: SessionCallResult.Payments,
        expectedResult: CheckoutResult,
    ) = runTest {
        whenever(checkoutCallback.beforeSubmit(any())) doReturn false
        whenever(sessionInteractor.submitPayment(any())) doReturn sessionResult

        val state = TestComponentState()
        val result = sessionsComponentEventHandler.onPaymentComponentEvent(PaymentComponentEvent.Submit(state))

        assertEquals(expectedResult, result)
    }

    companion object {

        @JvmStatic
        fun sessionInteractorSubmitSource() = listOf(
            // sessionResult, checkoutResult
            arguments(SessionCallResult.Payments.Action(TestAction()), CheckoutResult.Action(TestAction())),
            arguments(SessionCallResult.Payments.Error(Throwable()), CheckoutResult.Error()),
            arguments(
                SessionCallResult.Payments.Finished(SessionPaymentResult(null, null, null, null, null)),
                CheckoutResult.Finished(),
            ),
        )
    }
}
