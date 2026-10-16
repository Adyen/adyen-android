/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.api

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.SessionStateStore
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import com.adyen.checkout.dropin.internal.data.model.DisableTokenRequest
import com.adyen.checkout.dropin.internal.data.model.DisableTokenResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doSuspendableAnswer
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

@ExtendWith(MockitoExtension::class)
internal class DisableTokenRepositoryTest(
    @param:Mock private val disableTokenService: DisableTokenService,
    @param:Mock private val sessionStateStore: SessionStateStore,
) {

    private val checkoutSession = CheckoutSession(
        sessionSetupResponse = SessionSetupResponse(
            id = TEST_SESSION_ID,
            sessionData = "setup-session-data",
            amount = null,
            expiresAt = "2026-10-15T12:00:00Z",
            paymentMethods = null,
            returnUrl = null,
            configuration = null,
            shopperLocale = null,
        ),
        order = null,
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
    )

    private val repository = DisableTokenRepository(
        disableTokenService = disableTokenService,
        sessionStateStore = sessionStateStore,
    )

    @BeforeEach
    fun setup() {
        whenever(sessionStateStore.getSessionData(checkoutSession)) doReturn "stored-session-data"
    }

    @Test
    fun `when removing a stored payment method, then the session data from the store is sent`() = runTest {
        whenever(disableTokenService.disableToken(any(), any(), any())) doReturn DisableTokenResponse(null)

        repository.removeStoredPaymentMethod(checkoutSession, "spm-id")

        verify(disableTokenService).disableToken(
            request = DisableTokenRequest(sessionData = "stored-session-data", storedPaymentMethodId = "spm-id"),
            sessionId = TEST_SESSION_ID,
            clientKey = TEST_CLIENT_KEY,
        )
    }

    @Test
    fun `when the token is disabled, then success is returned and the new session data is stored`() = runTest {
        whenever(disableTokenService.disableToken(any(), any(), any())) doReturn
            DisableTokenResponse(sessionData = "updated-session-data")

        val result = repository.removeStoredPaymentMethod(checkoutSession, "spm-id")

        assertTrue(result.isSuccess)
        verify(sessionStateStore).setSessionData(TEST_SESSION_ID, "updated-session-data")
    }

    @Test
    fun `when the response has no session data, then success is returned and the stored session data is kept`() =
        runTest {
            whenever(disableTokenService.disableToken(any(), any(), any())) doReturn DisableTokenResponse(null)

            val result = repository.removeStoredPaymentMethod(checkoutSession, "spm-id")

            assertTrue(result.isSuccess)
            verify(sessionStateStore, never()).setSessionData(any(), any())
        }

    @Test
    fun `when the call fails, then failure is returned and the stored session data is kept`() = runTest {
        whenever(disableTokenService.disableToken(any(), any(), any())) doSuspendableAnswer {
            throw IOException("network down")
        }

        val result = repository.removeStoredPaymentMethod(checkoutSession, "spm-id")

        assertTrue(result.isFailure)
        verify(sessionStateStore, never()).setSessionData(any(), any())
    }

    companion object {
        private const val TEST_SESSION_ID = "session-id"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnmqwerty"
    }
}
