/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 3/6/2022.
 */

package com.adyen.checkout.sessions.repository

import app.cash.turbine.test
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.sessions.api.SessionService
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.orders.SessionBalanceResponse
import com.adyen.checkout.sessions.model.orders.SessionCancelOrderResponse
import com.adyen.checkout.sessions.model.orders.SessionOrderResponse
import com.adyen.checkout.sessions.model.payments.SessionDetailsResponse
import com.adyen.checkout.sessions.model.payments.SessionPaymentsResponse
import com.adyen.checkout.sessions.model.setup.SessionSetupResponse
import com.adyen.checkout.test.TestDispatcherExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockitoExtension::class, TestDispatcherExtension::class)
internal class SessionRepositoryTest(
    @Mock private val sessionService: SessionService,
) {

    private lateinit var sessionRepository: SessionRepository

    @BeforeEach
    fun before() {
        val initialSession = Session("id", "sessionData")
        sessionRepository = SessionRepository(sessionService, "someclientkey", initialSession)
    }

    @Nested
    @DisplayName("Session data is updated when")
    inner class SessionDataUpdateTest {

        @Test
        fun `session setup is successful`() = runTest {
            whenever(sessionService.setupSession(any(), any(), any())) doReturn SessionSetupResponse(
                id = "id",
                sessionData = "updatedSessionData",
                amount = null,
                expiresAt = "expiresAt",
                paymentMethods = null,
                returnUrl = "returnUrl",
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.setupSession(null)

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `submit payment is successful`() = runTest {
            whenever(sessionService.submitPayment(any(), any(), any())) doReturn SessionPaymentsResponse(
                sessionData = "updatedSessionData",
                status = null,
                resultCode = null,
                action = null,
                order = null,
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.submitPayment(PaymentComponentData())

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `submit details is successful`() = runTest {
            whenever(sessionService.submitDetails(any(), any(), any())) doReturn SessionDetailsResponse(
                sessionData = "updatedSessionData",
                status = null,
                resultCode = null,
                action = null,
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.submitDetails(ActionComponentData())

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `check balance is successful`() = runTest {
            whenever(sessionService.checkBalance(any(), any(), any())) doReturn SessionBalanceResponse(
                sessionData = "updatedSessionData",
                balance = Amount.EMPTY,
                transactionLimit = null,
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.checkBalance(CardPaymentMethod())

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `create order is successful`() = runTest {
            whenever(sessionService.createOrder(any(), any(), any())) doReturn SessionOrderResponse(
                sessionData = "updatedSessionData",
                orderData = "",
                pspReference = "",
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.createOrder()

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }

        @Test
        fun `cancel order is successful`() = runTest {
            whenever(sessionService.cancelOrder(any(), any(), any())) doReturn SessionCancelOrderResponse(
                sessionData = "updatedSessionData",
                status = null,
            )

            sessionRepository.sessionFlow.test {
                sessionRepository.cancelOrder(OrderRequest("", ""))

                skipItems(1)

                assertEquals("updatedSessionData", awaitItem().sessionData)

                cancelAndConsumeRemainingEvents()
            }
        }
    }
}
