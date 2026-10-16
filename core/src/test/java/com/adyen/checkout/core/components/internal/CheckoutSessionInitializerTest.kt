/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionResponse
import com.adyen.checkout.core.sessions.internal.InMemorySessionStateStore
import com.adyen.checkout.core.sessions.internal.data.api.SessionRepository
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException
import java.util.Locale

@ExtendWith(MockitoExtension::class)
internal class CheckoutSessionInitializerTest(
    @param:Mock private val sessionRepository: SessionRepository,
) {

    private val sessionStateStore = InMemorySessionStateStore()

    private val initializer = CheckoutSessionInitializer(
        sessionRepository = sessionRepository,
        sessionStateStore = sessionStateStore,
    )

    @Test
    fun `when initialize is called, then the session is set up with the values from the session response`() =
        runTest {
            whenever(sessionRepository.setupSession(any(), anyOrNull(), anyOrNull())) doReturn
                Result.failure(IOException())

            initializer.initialize(SessionResponse(TEST_SESSION_ID, "merchant-session-data"), createConfiguration())

            verify(sessionRepository).setupSession(TEST_SESSION_ID, "merchant-session-data", null)
        }

    @Test
    fun `when setup succeeds, then the checkout session is returned`() = runTest {
        val setupResponse = createSetupResponse()
        whenever(sessionRepository.setupSession(any(), anyOrNull(), anyOrNull())) doReturn Result.success(setupResponse)
        val configuration = createConfiguration()

        val result = initializer.initialize(SessionResponse(TEST_SESSION_ID, "merchant-session-data"), configuration)

        val expected = CheckoutSession(
            sessionSetupResponse = setupResponse.copy(amount = TEST_AMOUNT),
            order = null,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `when setup succeeds, then the session data from the setup is stored`() = runTest {
        whenever(sessionRepository.setupSession(any(), anyOrNull(), anyOrNull())) doReturn
            Result.success(createSetupResponse(sessionData = "setup-session-data"))

        initializer.initialize(SessionResponse(TEST_SESSION_ID, "merchant-session-data"), createConfiguration())

        assertEquals("setup-session-data", getStoredSessionData())
    }

    @Test
    fun `when session data is already stored for the session, then setup replaces it`() = runTest {
        sessionStateStore.setSessionData(TEST_SESSION_ID, "restored-session-data")
        whenever(sessionRepository.setupSession(any(), anyOrNull(), anyOrNull())) doReturn
            Result.success(createSetupResponse(sessionData = "setup-session-data"))

        initializer.initialize(SessionResponse(TEST_SESSION_ID, "merchant-session-data"), createConfiguration())

        assertEquals("setup-session-data", getStoredSessionData())
    }

    @Test
    fun `when setup fails, then null is returned and the stored session data is unchanged`() = runTest {
        sessionStateStore.setSessionData(TEST_SESSION_ID, "restored-session-data")
        whenever(sessionRepository.setupSession(any(), anyOrNull(), anyOrNull())) doReturn Result.failure(IOException())

        val result = initializer.initialize(
            SessionResponse(TEST_SESSION_ID, "merchant-session-data"),
            createConfiguration(),
        )

        assertNull(result)
        assertEquals("restored-session-data", getStoredSessionData())
    }

    private fun createConfiguration() = CheckoutConfiguration(
        environment = Environment.TEST,
        clientKey = TEST_CLIENT_KEY,
        shopperLocale = Locale.US,
        amount = TEST_AMOUNT,
    )

    // Reads through a session with different setup data, so a value that was never stored can't pass as stored.
    private fun getStoredSessionData(): String = sessionStateStore.getSessionData(
        CheckoutSession(
            sessionSetupResponse = createSetupResponse(sessionData = "not-stored-session-data"),
            order = null,
            environment = Environment.TEST,
            clientKey = TEST_CLIENT_KEY,
        ),
    )

    private fun createSetupResponse(
        sessionData: String = "setup-session-data",
    ) = SessionSetupResponse(
        id = TEST_SESSION_ID,
        sessionData = sessionData,
        amount = Amount(currency = "USD", value = 1L),
        expiresAt = "2026-10-15T12:00:00Z",
        paymentMethods = null,
        returnUrl = null,
        configuration = null,
        shopperLocale = null,
    )

    companion object {
        private const val TEST_SESSION_ID = "session-id"
        private const val TEST_CLIENT_KEY = "test_qwertyuiopasdfghjklzxcvbnm"
        private val TEST_AMOUNT = Amount(currency = "EUR", value = 1000L)
    }
}
