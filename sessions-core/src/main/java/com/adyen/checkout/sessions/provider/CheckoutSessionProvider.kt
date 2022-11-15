/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions.provider

import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.sessions.model.SessionModel
import kotlinx.coroutines.CoroutineScope

// TODO docs
object CheckoutSessionProvider {
    // TODO docs
    suspend fun createSession(
        sessionModel: SessionModel,
        configuration: Configuration,
    ): CheckoutSessionResult {
        return CheckoutSessionInitializer(sessionModel, configuration).setupSession()
    }

    // TODO docs
    // callback alternative for the suspend function
    fun createSession(
        coroutineScope: CoroutineScope,
        sessionModel: SessionModel,
        configuration: Configuration,
        callback: (CheckoutSessionResult) -> Unit
    ) {
        CheckoutSessionInitializer(sessionModel, configuration).setupSession(coroutineScope, callback)
    }
}
