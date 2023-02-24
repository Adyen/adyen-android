/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/1/2023.
 */

package com.adyen.checkout.sessions.core.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.core.internal.SavedStateHandleContainer
import com.adyen.checkout.components.core.internal.SavedStateHandleProperty
import com.adyen.checkout.core.internal.util.LogUtil
import com.adyen.checkout.sessions.core.CheckoutSession
import com.adyen.checkout.sessions.core.SessionModel
import com.adyen.checkout.sessions.core.internal.data.model.SessionDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToDetails
import com.adyen.checkout.sessions.core.internal.data.model.mapToModel

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionSavedStateHandleContainer(
    override val savedStateHandle: SavedStateHandle,
    checkoutSession: CheckoutSession
) : SavedStateHandleContainer {

    private var sessionDetails: SessionDetails? by SavedStateHandleProperty(SESSION_KEY)
    var isFlowTakenOver: Boolean? by SavedStateHandleProperty(IS_SESSIONS_FLOW_TAKEN_OVER_KEY)

    init {
        if (sessionDetails == null) {
            sessionDetails = checkoutSession.sessionSetupResponse.mapToDetails()
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
        private val TAG = LogUtil.getTag()

        private const val SESSION_KEY = "SESSION_KEY"
        private const val IS_SESSIONS_FLOW_TAKEN_OVER_KEY = "IS_SESSIONS_FLOW_TAKEN_OVER_KEY"
    }
}
