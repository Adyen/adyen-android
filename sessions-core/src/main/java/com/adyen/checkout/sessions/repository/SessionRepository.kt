/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/3/2022.
 */

package com.adyen.checkout.sessions.repository

import com.adyen.checkout.components.api.suspendedCall
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.sessions.api.SessionSetupConnection
import com.adyen.checkout.sessions.model.Session
import com.adyen.checkout.sessions.model.request.SessionSetupRequest
import com.adyen.checkout.sessions.model.response.SessionSetupResponse
import java.io.IOException
import org.json.JSONException

class SessionRepository {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    suspend fun setupSession(
        configuration: Configuration,
        session: Session,
        order: OrderRequest?
    ): SessionSetupResponse {
        Logger.d(TAG, "Setting up session")
        try {
            val request = SessionSetupRequest(session.sessionData.orEmpty(), order)
            return SessionSetupConnection(
                request = request,
                environment = configuration.environment,
                sessionId = session.id,
                clientKey = configuration.clientKey
            ).suspendedCall()
        } catch (e: IOException) {
            Logger.e(TAG, "SessionSetupConnection Failed", e)
            throw CheckoutException("Unable to setup session")
        } catch (e: JSONException) {
            Logger.e(TAG, "SessionSetupConnection unexpected result", e)
            throw CheckoutException("Unable to setup session")
        }
    }
}
