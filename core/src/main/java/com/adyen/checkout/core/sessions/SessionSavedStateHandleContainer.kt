/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.sessions

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.internal.SavedStateHandleContainer
import com.adyen.checkout.core.internal.SavedStateHandleProperty
import com.adyen.checkout.core.sessions.internal.data.model.SessionDetails
import com.adyen.checkout.core.sessions.internal.data.model.mapToDetails
import com.adyen.checkout.core.sessions.internal.data.model.mapToModel

class SessionSavedStateHandleContainer(
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

    fun getSessionModel(): SessionModel {
        return requireNotNull(sessionDetails).mapToModel()
    }

    companion object {

        private const val SESSION_KEY = "SESSION_KEY"
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
    }
}
