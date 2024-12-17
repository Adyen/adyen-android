/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/2/2023.
 */

package com.adyen.checkout.sessions.core.internal

import app.cash.turbine.test
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.BalanceResult
import com.adyen.checkout.components.core.OrderRequest
import com.adyen.checkout.components.core.OrderResponse
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.action.RedirectAction
import com.adyen.checkout.components.core.internal.analytics.ErrorEvent
import com.adyen.checkout.components.core.internal.analytics.GenericEvents
import com.adyen.checkout.components.core.internal.analytics.TestAnalyticsManager
import com.adyen.checkout.components.core.internal.util.StatusResponseUtils
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.SessionPaymentResult
import com.adyen.checkout.sessions.core.SessionSetupConfiguration
import com.adyen.checkout.sessions.core.SessionSetupResponse
import com.adyen.checkout.sessions.core.TestComponentState
import com.adyen.checkout.sessions.core.TestPaymentMethod
import com.adyen.checkout.sessions.core.internal.data.api.SessionRepository
import com.adyen.checkout.sessions.core.internal.data.model.SessionBalanceResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionCancelOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetailsResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionDisableTokenResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionOrderResponse
import com.adyen.checkout.sessions.core.internal.data.model.SessionPaymentsResponse
import com.adyen.checkout.test.LoggingExtension
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class, LoggingExtension::class)
internal class SessionInteractorTest(
    @Mock private val sessionRepository: SessionRepository,
) {

    private lateinit var sessionInteractor: SessionInteractor
    private lateinit var analyticsManager: TestAnalyticsManager

    @BeforeEach
    fun before() {
        analyticsManager = TestAnalyticsManager()
        sessionInteractor = createSessionInteractor()
    }

    @Nested
    @DisplayName("when a payments call is requested and")
    inner class PaymentsCallTest {

        @Nested
        @DisplayName("flow is not taken over and")
        inner class FlowNotTakenOverTest {

            @Test
            fun `payment is completed then Finished is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = createSessionPaymentsResponse()

                    whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                    val expectedResult = SessionCallResult.Payments.Finished(
                        SessionPaymentResult(
                            sessionId = TEST_SESSION_ID,
                            sessionResult = mockResponse.sessionResult,
                            sessionData = mockResponse.sessionData,
                            resultCode = mockResponse.resultCode,
                            order = mockResponse.order,
                        ),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `action is required then Action is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = createSessionPaymentsResponse(
                        action = RedirectAction(),
                    )

                    whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                    val expectedResult = SessionCallResult.Payments.Action(
                        requireNotNull(mockResponse.action),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `a partial payment is not fully paid then Finished is returned and session data is updated`() =
                runTest {
                    sessionInteractor.sessionFlow.test {
                        val mockResponse = createSessionPaymentsResponse(
                            order = TEST_ORDER_RESPONSE,
                        )

                        whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.success(mockResponse)

                        val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                        val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                        val expectedResult = SessionCallResult.Payments.NotFullyPaidOrder(
                            SessionPaymentResult(
                                sessionId = TEST_SESSION_ID,
                                sessionResult = mockResponse.sessionResult,
                                sessionData = mockResponse.sessionData,
                                resultCode = mockResponse.resultCode,
                                order = mockResponse.order,
                            ),
                        )

                        assertEquals(expectedResult, result)
                        assertEquals(expectedSessionModel, expectMostRecentItem())
                    }
                }

            @Test
            fun `a partial payment is fully paid then Finished is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = createSessionPaymentsResponse(
                        order = TEST_ORDER_RESPONSE.copy(remainingAmount = Amount("USD", 0)),
                    )

                    whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                    val expectedResult = SessionCallResult.Payments.Finished(
                        SessionPaymentResult(
                            sessionId = TEST_SESSION_ID,
                            sessionResult = mockResponse.sessionResult,
                            sessionData = mockResponse.sessionData,
                            resultCode = mockResponse.resultCode,
                            order = mockResponse.order,
                        ),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `a partial payment is refused then RefusedPartialPayment is returned and session data is updated`() =
                runTest {
                    sessionInteractor.sessionFlow.test {
                        val mockResponse = createSessionPaymentsResponse(
                            order = TEST_ORDER_RESPONSE,
                            resultCode = StatusResponseUtils.RESULT_REFUSED,
                        )

                        whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.success(mockResponse)

                        val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                        val expectedResult = SessionCallResult.Payments.RefusedPartialPayment(
                            SessionPaymentResult(
                                sessionId = TEST_SESSION_ID,
                                sessionResult = mockResponse.sessionResult,
                                sessionData = mockResponse.sessionData,
                                resultCode = mockResponse.resultCode,
                                order = mockResponse.order,
                            ),
                        )

                        assertEquals(expectedResult, result)

                        val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                        assertEquals(expectedSessionModel, expectMostRecentItem())
                    }
                }

            @Test
            fun `an error is thrown then Error is returned`() = runTest {
                val exception = Exception("failed for testing")

                whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.failure(exception)

                val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "")

                val expectedResult = SessionCallResult.Payments.Error(exception)

                assertEquals(expectedResult, result)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver is set to true`() = runTest {
                val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { true }, "")
                val expectedResult = SessionCallResult.Payments.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }

        @Nested
        @DisplayName("flow is taken over and")
        inner class FlowTakenOverTest {

            @BeforeEach
            fun before() {
                sessionInteractor = createSessionInteractor(isFlowTakenOver = true)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver stays true`() = runTest {
                val result = sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { true }, "")
                val expectedResult = SessionCallResult.Payments.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }

            @Test
            fun `merchant doesn't handle call then an exception is thrown and isFlowTakenOver stays true`() = runTest {
                assertThrows<CheckoutException>(
                    "Sessions flow was already taken over in a previous call, makePayment should be implemented",
                ) {
                    sessionInteractor.onPaymentsCallRequested(TEST_COMPONENT_STATE, { false }, "makePayment")
                }

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }
    }

    @Nested
    @DisplayName("when a details call is requested and")
    inner class DetailsCallTest {

        @Nested
        @DisplayName("flow is not taken over and")
        inner class FlowNotTakenOverTest {

            @Test
            fun `payment is completed then Finished is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = createSessionDetailsResponse(order = TEST_ORDER_RESPONSE)

                    whenever(sessionRepository.submitDetails(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.onDetailsCallRequested(ActionComponentData(), { false }, "")

                    val expectedResult = SessionCallResult.Details.Finished(
                        SessionPaymentResult(
                            sessionId = TEST_SESSION_ID,
                            sessionResult = mockResponse.sessionResult,
                            sessionData = mockResponse.sessionData,
                            resultCode = mockResponse.resultCode,
                            order = mockResponse.order,
                        ),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `action is required then Action is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = createSessionDetailsResponse(
                        action = RedirectAction(),
                    )

                    whenever(sessionRepository.submitDetails(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.onDetailsCallRequested(ActionComponentData(), { false }, "")

                    val expectedResult = SessionCallResult.Details.Action(
                        requireNotNull(mockResponse.action),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `an error is thrown then Error is returned`() = runTest {
                val exception = Exception("failed for testing")

                whenever(sessionRepository.submitDetails(any(), any())) doReturn Result.failure(exception)

                val result = sessionInteractor.onDetailsCallRequested(ActionComponentData(), { false }, "")

                val expectedResult = SessionCallResult.Details.Error(exception)

                assertEquals(expectedResult, result)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver is set to true`() = runTest {
                val result = sessionInteractor.onDetailsCallRequested(ActionComponentData(), { true }, "")
                val expectedResult = SessionCallResult.Details.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }

        @Nested
        @DisplayName("flow is taken over and")
        inner class FlowTakenOverTest {

            @BeforeEach
            fun before() {
                sessionInteractor = createSessionInteractor(isFlowTakenOver = true)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver stays true`() = runTest {
                val result = sessionInteractor.onDetailsCallRequested(ActionComponentData(), { true }, "")
                val expectedResult = SessionCallResult.Details.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }

            @Test
            fun `merchant doesn't handle call then an exception is thrown and isFlowTakenOver stays true`() = runTest {
                assertThrows<CheckoutException>(
                    "Sessions flow was already taken over in a previous call, makeDetails should be implemented",
                ) {
                    sessionInteractor.onDetailsCallRequested(ActionComponentData(), { false }, "makeDetails")
                }

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }
    }

    @Nested
    @DisplayName("when a balance call is requested and")
    inner class BalanceCallTest {

        @Nested
        @DisplayName("flow is not taken over and")
        inner class FlowNotTakenOverTest {

            @Test
            fun `balance is positive then Successful is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = SessionBalanceResponse(
                        sessionData = "session_data_updated",
                        balance = Amount("USD", 5000),
                        transactionLimit = Amount("USD", 1000),
                    )

                    whenever(sessionRepository.checkBalance(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.checkBalance(TEST_COMPONENT_STATE, { false }, "")

                    val expectedResult = SessionCallResult.Balance.Successful(
                        BalanceResult(
                            balance = mockResponse.balance,
                            transactionLimit = mockResponse.transactionLimit,
                        ),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `an error is thrown then Error is returned`() = runTest {
                val exception = Exception("failed for testing")

                whenever(sessionRepository.checkBalance(any(), any())) doReturn Result.failure(exception)

                val result = sessionInteractor.checkBalance(TEST_COMPONENT_STATE, { false }, "")

                val expectedResult = SessionCallResult.Balance.Error(exception)

                assertEquals(expectedResult, result)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver is set to true`() = runTest {
                val result = sessionInteractor.checkBalance(TEST_COMPONENT_STATE, { true }, "")
                val expectedResult = SessionCallResult.Balance.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }

        @Nested
        @DisplayName("flow is taken over and")
        inner class FlowTakenOverTest {

            @BeforeEach
            fun before() {
                sessionInteractor = createSessionInteractor(isFlowTakenOver = true)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver stays true`() = runTest {
                val result = sessionInteractor.checkBalance(TEST_COMPONENT_STATE, { true }, "")
                val expectedResult = SessionCallResult.Balance.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }

            @Test
            fun `merchant doesn't handle call then an exception is thrown and isFlowTakenOver stays true`() = runTest {
                assertThrows<CheckoutException>(
                    "Sessions flow was already taken over in a previous call, makeBalance should be implemented",
                ) {
                    sessionInteractor.checkBalance(TEST_COMPONENT_STATE, { false }, "makeBalance")
                }

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }
    }

    @Nested
    @DisplayName("when a create order call is requested and")
    inner class CreateOrderCallTest {

        @Nested
        @DisplayName("flow is not taken over and")
        inner class FlowNotTakenOverTest {

            @Test
            fun `order is created then Successful is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = SessionOrderResponse(
                        sessionData = "session_data_updated",
                        orderData = "orderData",
                        pspReference = "pspReference",
                    )

                    whenever(sessionRepository.createOrder(any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.createOrder({ false }, "")

                    val expectedResult = SessionCallResult.CreateOrder.Successful(
                        OrderResponse(
                            pspReference = mockResponse.pspReference,
                            orderData = mockResponse.orderData,
                            amount = null,
                            remainingAmount = null,
                        ),
                    )

                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `an error is thrown then Error is returned`() = runTest {
                val exception = Exception("failed for testing")

                whenever(sessionRepository.createOrder(any())) doReturn Result.failure(exception)

                val result = sessionInteractor.createOrder({ false }, "")

                val expectedResult = SessionCallResult.CreateOrder.Error(exception)

                assertEquals(expectedResult, result)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver is set to true`() = runTest {
                val result = sessionInteractor.createOrder({ true }, "")
                val expectedResult = SessionCallResult.CreateOrder.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }

        @Nested
        @DisplayName("flow is taken over and")
        inner class FlowTakenOverTest {

            @BeforeEach
            fun before() {
                sessionInteractor = createSessionInteractor(isFlowTakenOver = true)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver stays true`() = runTest {
                val result = sessionInteractor.createOrder({ true }, "")
                val expectedResult = SessionCallResult.CreateOrder.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }

            @Test
            fun `merchant doesn't handle call then an exception is thrown and isFlowTakenOver stays true`() = runTest {
                assertThrows<CheckoutException>(
                    "Sessions flow was already taken over in a previous call, createOrder should be implemented",
                ) {
                    sessionInteractor.createOrder({ false }, "createOrder")
                }

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }
    }

    @Nested
    @DisplayName("when a cancel order call is requested and")
    inner class CancelOrderCallTest {

        @Nested
        @DisplayName("flow is not taken over and")
        inner class FlowNotTakenOverTest {

            @Test
            fun `order is cancelled then Successful is returned and session data is updated`() = runTest {
                sessionInteractor.sessionFlow.test {
                    val mockResponse = SessionCancelOrderResponse(
                        sessionData = "session_data_updated",
                        status = "status",
                    )

                    whenever(sessionRepository.cancelOrder(any(), any())) doReturn Result.success(mockResponse)

                    val result = sessionInteractor.cancelOrder(TEST_ORDER_REQUEST, { false }, "")

                    val expectedResult = SessionCallResult.CancelOrder.Successful
                    assertEquals(expectedResult, result)

                    val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                    assertEquals(expectedSessionModel, expectMostRecentItem())
                }
            }

            @Test
            fun `an error is thrown then Error is returned`() = runTest {
                val exception = Exception("failed for testing")

                whenever(sessionRepository.cancelOrder(any(), any())) doReturn Result.failure(exception)

                val result = sessionInteractor.cancelOrder(TEST_ORDER_REQUEST, { false }, "")

                val expectedResult = SessionCallResult.CancelOrder.Error(exception)

                assertEquals(expectedResult, result)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver is set to true`() = runTest {
                val result = sessionInteractor.cancelOrder(TEST_ORDER_REQUEST, { true }, "")
                val expectedResult = SessionCallResult.CancelOrder.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }

        @Nested
        @DisplayName("flow is taken over and")
        inner class FlowTakenOverTest {

            @BeforeEach
            fun before() {
                sessionInteractor = createSessionInteractor(isFlowTakenOver = true)
            }

            @Test
            fun `merchant handles call then TakenOver is returned and isFlowTakenOver stays true`() = runTest {
                val result = sessionInteractor.cancelOrder(TEST_ORDER_REQUEST, { true }, "")
                val expectedResult = SessionCallResult.CancelOrder.TakenOver
                assertEquals(expectedResult, result)

                assertTrue(sessionInteractor.isFlowTakenOver)
            }

            @Test
            fun `merchant doesn't handle call then an exception is thrown and isFlowTakenOver stays true`() = runTest {
                assertThrows<CheckoutException>(
                    "Sessions flow was already taken over in a previous call, cancelOrder should be implemented",
                ) {
                    sessionInteractor.cancelOrder(TEST_ORDER_REQUEST, { false }, "cancelOrder")
                }

                assertTrue(sessionInteractor.isFlowTakenOver)
            }
        }
    }

    @Nested
    @DisplayName("when an update payment methods call is requested and")
    inner class UpdatePaymentMethodsCallTest {

        @Test
        fun `payment methods are fetched then Successful is returned`() = runTest {
            val mockResponse = createSessionSetupResponse()

            whenever(sessionRepository.setupSession(any(), any())) doReturn Result.success(mockResponse)

            val result = sessionInteractor.updatePaymentMethods(TEST_ORDER_RESPONSE)

            val expectedResult = SessionCallResult.UpdatePaymentMethods.Successful(
                paymentMethods = requireNotNull(mockResponse.paymentMethodsApiResponse),
                order = TEST_ORDER_RESPONSE,
            )
            assertEquals(expectedResult, result)
        }

        @Test
        fun `payment methods are null then Error is returned`() = runTest {
            val mockResponse = createSessionSetupResponse(paymentMethods = null)

            whenever(sessionRepository.setupSession(any(), any())) doReturn Result.success(mockResponse)

            val result = sessionInteractor.updatePaymentMethods(TEST_ORDER_RESPONSE)

            assertTrue(result is SessionCallResult.UpdatePaymentMethods.Error)
            require(result is SessionCallResult.UpdatePaymentMethods.Error)

            assertTrue(result.throwable is CheckoutException)
            require(result.throwable is CheckoutException)

            assertEquals("Payment methods should not be null", result.throwable.message)
        }

        @Test
        fun `an error is thrown then Error is returned`() = runTest {
            val exception = Exception("failed for testing")

            whenever(sessionRepository.setupSession(any(), any())) doReturn Result.failure(exception)

            val result = sessionInteractor.updatePaymentMethods(TEST_ORDER_RESPONSE)

            val expectedResult = SessionCallResult.UpdatePaymentMethods.Error(exception)

            assertEquals(expectedResult, result)
        }
    }

    @Nested
    @DisplayName("when disable token call is requested and")
    inner class RemoveStoredPaymentMethodCallTest {

        @Test
        fun `it is successful then session data is updated`() = runTest {
            sessionInteractor.sessionFlow.test {
                val mockResponse = SessionDisableTokenResponse(sessionData = "session_data_updated")
                whenever(sessionRepository.disableToken(any(), any())) doReturn Result.success(mockResponse)

                val result = sessionInteractor.removeStoredPaymentMethod("stored_payment_method_id")

                val expectedResult = SessionCallResult.RemoveStoredPaymentMethod.Successful
                assertEquals(expectedResult, result)

                val expectedSessionModel = TEST_SESSION_MODEL.copy(sessionData = mockResponse.sessionData)
                assertEquals(expectedSessionModel, expectMostRecentItem())
            }
        }

        @Test
        fun `an error is thrown then Error is returned`() = runTest {
            val exception = Exception("failed for testing")

            whenever(sessionRepository.disableToken(any(), any())) doReturn Result.failure(exception)

            val result = sessionInteractor.removeStoredPaymentMethod("stored_payment_method_id")

            val expectedResult = SessionCallResult.RemoveStoredPaymentMethod.Error(exception)
            assertEquals(expectedResult, result)
        }
    }

    @Nested
    inner class AnalyticsTest {

        @Test
        fun `when payment calls returns an error then error event is tracked`() = runTest {
            val exception = Exception("failed for testing")
            val paymentMethodType = "scheme"
            val componentState = TestComponentState(
                data = PaymentComponentData(
                    TestPaymentMethod(type = paymentMethodType),
                    TEST_ORDER_REQUEST,
                    TEST_AMOUNT,
                ),
                isInputValid = true,
                isReady = true,
            )
            whenever(sessionRepository.submitPayment(any(), any())) doReturn Result.failure(exception)

            sessionInteractor.onPaymentsCallRequested(componentState, { false }, "")

            val expectedEvent = GenericEvents.error(
                component = paymentMethodType,
                event = ErrorEvent.API_PAYMENTS,
            )
            analyticsManager.assertLastEventEquals(expectedEvent)
        }
    }

    private fun createSessionInteractor(
        sessionModel: SessionModel = TEST_SESSION_MODEL,
        isFlowTakenOver: Boolean = false
    ): SessionInteractor {
        return SessionInteractor(
            sessionRepository = sessionRepository,
            sessionModel = sessionModel,
            isFlowTakenOver = isFlowTakenOver,
            analyticsManager = analyticsManager,
        )
    }

    private fun createSessionPaymentsResponse(
        resultCode: String? = null,
        action: Action? = null,
        order: OrderResponse? = null,
    ): SessionPaymentsResponse {
        return SessionPaymentsResponse(
            sessionResult = "session_result",
            sessionData = "session_data_updated",
            status = null,
            resultCode = resultCode,
            order = order,
            action = action,
        )
    }

    private fun createSessionDetailsResponse(
        resultCode: String? = null,
        action: Action? = null,
        order: OrderResponse? = null,
    ): SessionDetailsResponse {
        return SessionDetailsResponse(
            sessionResult = "session_result",
            sessionData = "session_data_updated",
            status = null,
            resultCode = resultCode,
            order = order,
            action = action,
        )
    }

    @Suppress("LongParameterList")
    private fun createSessionSetupResponse(
        id: String = "id",
        sessionData: String = "session_data_updated",
        amount: Amount? = null,
        expiresAt: String = "",
        returnUrl: String = "",
        paymentMethods: PaymentMethodsApiResponse? = PaymentMethodsApiResponse(),
        configuration: SessionSetupConfiguration? = null,
        shopperLocale: String? = null,
    ): SessionSetupResponse {
        return SessionSetupResponse(
            id = id,
            sessionData = sessionData,
            amount = amount,
            expiresAt = expiresAt,
            paymentMethodsApiResponse = paymentMethods,
            returnUrl = returnUrl,
            configuration = configuration,
            shopperLocale = shopperLocale,
        )
    }

    companion object {
        private const val TEST_SESSION_ID = "session_id"

        private val TEST_SESSION_MODEL = SessionModel(
            id = "session_id",
            sessionData = "session_data_initial",
        )

        private val TEST_ORDER_REQUEST = OrderRequest(
            pspReference = "PSP",
            orderData = "ORDER_DATA",
        )

        private val TEST_ORDER_RESPONSE = OrderResponse(
            pspReference = "PSP",
            orderData = "ORDER_DATA",
            amount = Amount("USD", 1337),
            remainingAmount = Amount("USD", 100),
        )

        private val TEST_AMOUNT = Amount("USD", 1337)

        private val TEST_COMPONENT_STATE = TestComponentState(
            data = PaymentComponentData(TestPaymentMethod(), TEST_ORDER_REQUEST, TEST_AMOUNT),
            isInputValid = true,
            isReady = true,
        )
    }
}
