/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.core.sessions.internal

import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.data.model.SessionSetupResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class InMemorySessionStateStoreTest {

    private val store = InMemorySessionStateStore()

    @Test
    fun `when nothing was set, then the session data from the setup is returned`() {
        assertEquals("setup-session-data", store.getSessionData(createCheckoutSession()))
    }

    @Test
    fun `when session data is set, then it is returned`() {
        store.setSessionData(SESSION_ID, "session-data")

        assertEquals("session-data", store.getSessionData(createCheckoutSession()))
    }

    @Test
    fun `when session data is set again, then the latest value is returned`() {
        store.setSessionData(SESSION_ID, "session-data")

        store.setSessionData(SESSION_ID, "updated-session-data")

        assertEquals("updated-session-data", store.getSessionData(createCheckoutSession()))
    }

    @Test
    fun `when session data is set for another session, then the session data from the setup is returned`() {
        store.setSessionData("other-session-id", "session-data")

        assertEquals("setup-session-data", store.getSessionData(createCheckoutSession()))
    }

    @Test
    fun `when two sessions set session data, then each returns its own`() {
        store.setSessionData(SESSION_ID, "session-data")
        store.setSessionData("other-session-id", "other-session-data")

        assertEquals("session-data", store.getSessionData(createCheckoutSession()))
        assertEquals("other-session-data", store.getSessionData(createCheckoutSession(id = "other-session-id")))
    }

    private fun createCheckoutSession(id: String = SESSION_ID) = CheckoutSession(
        sessionSetupResponse = SessionSetupResponse(
            id = id,
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
        clientKey = "test_qwertyuiopasdfghjklzxcvbnm",
    )

    companion object {
        private const val SESSION_ID = "session-id"
    }
}
