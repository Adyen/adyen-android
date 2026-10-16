/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2026.
 */

package com.adyen.checkout.dropin.internal.data.api

import com.adyen.checkout.core.common.internal.helper.runSuspendCatching
import com.adyen.checkout.core.sessions.CheckoutSession
import com.adyen.checkout.core.sessions.internal.SessionStateStore
import com.adyen.checkout.dropin.internal.data.model.DisableTokenRequest

/**
 * Removes stored payment methods of a session. The `sessionData` is read from and written to [sessionStateStore], so
 * these calls and the payment calls of the same session always send the latest value.
 */
internal class DisableTokenRepository(
    private val disableTokenService: DisableTokenService,
    private val sessionStateStore: SessionStateStore,
) {

    suspend fun removeStoredPaymentMethod(
        checkoutSession: CheckoutSession,
        storedPaymentMethodId: String,
    ): Result<Unit> = runSuspendCatching {
        val sessionId = checkoutSession.sessionSetupResponse.id
        val request = DisableTokenRequest(
            sessionData = sessionStateStore.getSessionData(checkoutSession),
            storedPaymentMethodId = storedPaymentMethodId,
        )
        val response = disableTokenService.disableToken(request, sessionId, checkoutSession.clientKey)
        response.sessionData?.let { sessionStateStore.setSessionData(sessionId, it) }
    }
}
