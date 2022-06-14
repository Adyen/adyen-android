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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
internal class SessionRepositoryTest {

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule()

    @Mock
    private lateinit var sessionService: SessionService

    private lateinit var sessionRepository: SessionRepository

    @Before
    fun before() {
        val initialSession = Session("id", "sessionData")
        sessionRepository = SessionRepository(sessionService, "someclientkey", initialSession)

        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun `whenever session setup is successful, then session data is updated`() = runTest {
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
    fun `whenever submit payment is successful, then session data is updated`() = runTest {
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
    fun `whenever submit details is successful, then session data is updated`() = runTest {
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
    fun `whenever check balance is successful, then session data is updated`() = runTest {
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
    fun `whenever create order is successful, then session data is updated`() = runTest {
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
    fun `whenever cancel order is successful, then session data is updated`() = runTest {
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
