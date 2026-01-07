/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.common.internal.SavedStateHandleContainer
import com.adyen.checkout.core.common.internal.SavedStateHandleProperty
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.SessionResponse
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetails
import com.adyen.checkout.core.sessions.internal.data.model.mapToDetails
import com.adyen.checkout.core.sessions.internal.data.model.mapToSessionResponse

internal class SessionSavedStateHandleContainer(
    override val savedStateHandle: SavedStateHandle,
    checkoutSession: CheckoutSession
) : SavedStateHandleContainer {

    private var sessionDetails: SessionDetails? by SavedStateHandleProperty(SESSION_KEY)
    var isFlowTakenOver: Boolean? by SavedStateHandleProperty(IS_SESSIONS_FLOW_TAKEN_OVER_KEY)

    init {
        if (sessionDetails == null) {
            sessionDetails = checkoutSession.mapToDetails()
        }
        if (isFlowTakenOver == null) {
            isFlowTakenOver = false
        }
    }

    fun updateSessionData(sessionData: String) {
        sessionDetails = sessionDetails?.copy(sessionData = sessionData)
    }

    fun getSessionResponse(): SessionResponse {
        return requireNotNull(sessionDetails).mapToSessionResponse()
    }

    companion object {

        private const val SESSION_KEY = "SESSION_KEY"
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
    }
}
