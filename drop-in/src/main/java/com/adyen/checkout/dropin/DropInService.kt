/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2026.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.dropin.internal.service.DropInBinder

abstract class DropInService : LifecycleService() {

    private val binder = object : DropInBinder() {
        override suspend fun requestOnSubmit(state: PaymentComponentState<*>): CheckoutResult {
            return onSubmit(state)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    protected abstract suspend fun onSubmit(state: PaymentComponentState<*>): CheckoutResult
}
