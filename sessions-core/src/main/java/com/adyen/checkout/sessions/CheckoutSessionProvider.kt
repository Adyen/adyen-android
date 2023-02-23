/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/11/2022.
 */

package com.adyen.checkout.sessions

import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.Order
import com.adyen.checkout.sessions.internal.CheckoutSessionInitializer
import kotlinx.coroutines.CoroutineScope

// TODO docs
object CheckoutSessionProvider {
    // TODO docs
    suspend fun createSession(
        sessionModel: SessionModel,
        configuration: Configuration,
        order: Order? = null,
    ): CheckoutSessionResult {
        return CheckoutSessionInitializer(sessionModel, configuration, order).setupSession()
    }

    // TODO docs
    // callback alternative for the suspend function
    fun createSession(
        coroutineScope: CoroutineScope,
        sessionModel: SessionModel,
        configuration: Configuration,
        order: Order? = null,
        callback: (CheckoutSessionResult) -> Unit
    ) {
        CheckoutSessionInitializer(sessionModel, configuration, order).setupSession(coroutineScope, callback)
    }
}
