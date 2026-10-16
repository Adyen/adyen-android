/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.core.sessions.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.sessions.CheckoutSession
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds the latest `sessionData` of each session, keyed by the session ID.
 *
 * Every `/sessions` call returns a new `sessionData` that the next call has to send. A session can be used by several
 * flows, for example multiple controllers created from the same context, or Drop-in making its own calls. Sharing the
 * value through this store makes sure each of them sends the latest one. Drop-in also restores its session data into
 * this store after process death.
 *
 * Reading and writing a single value is atomic, but a whole call is not. When two flows of the same session make a call
 * at the same time, both send the same value and the response that arrives last is kept.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface SessionStateStore {

    /**
     * @return The latest `sessionData` of [checkoutSession], or the one from its setup if none was set in this process.
     */
    fun getSessionData(checkoutSession: CheckoutSession): String

    fun setSessionData(sessionId: String, sessionData: String)
}

/**
 * The [SessionStateStore] shared by everything in this process.
 */
// TODO - The store only lives in memory. Drop-in restores its session data into it after process death, how components
//  restore theirs is still open.
// TODO - Remove the state of finished or expired sessions, once it is decided how the state is restored.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object DefaultSessionStateStore : SessionStateStore by InMemorySessionStateStore()

internal class InMemorySessionStateStore : SessionStateStore {

    private val sessionDataById = ConcurrentHashMap<String, String>()

    // Setup seeds the store, so the fallback is only used when the store is empty after process death.
    // TODO - Once Drop-in and components restore their session data into the store before using it, remove the
    //  fallback to the session data from the setup.
    override fun getSessionData(checkoutSession: CheckoutSession): String {
        val setupResponse = checkoutSession.sessionSetupResponse
        return sessionDataById[setupResponse.id] ?: setupResponse.sessionData
    }

    override fun setSessionData(sessionId: String, sessionData: String) {
        sessionDataById[sessionId] = sessionData
    }
}
