/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/1/2023.
 */

package com.adyen.checkout.sessions

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class SessionHandler(
    private val checkoutSession: CheckoutSession,
    private val savedStateHandle: SavedStateHandle,
) {

    fun onPaymentComponentEvent(event: PaymentComponentEvent<*>) {
        Logger.e(TAG, "Event received $event") // TODO sessions: remove
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
