/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.sessions.data.api

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.util.runSuspendCatching
import com.adyen.checkout.core.sessions.SessionModel
import com.adyen.checkout.core.sessions.data.SessionSetupRequest
import com.adyen.checkout.core.sessions.data.SessionSetupResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionRepository(
    private val sessionService: SessionService,
    private val clientKey: String,
) {

    @Suppress("CommentWrapping")
    suspend fun setupSession(
        sessionModel: SessionModel,
        // TODO - Partial Payment Flow
//        order: OrderRequest?
    ): Result<SessionSetupResponse> = runSuspendCatching {
        val request = SessionSetupRequest(sessionModel.sessionData.orEmpty())
        sessionService.setupSession(
            request = request,
            sessionId = sessionModel.id,
            clientKey = clientKey,
        )
    }
}
