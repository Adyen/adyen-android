/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2026.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import androidx.lifecycle.LifecycleService
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.dropin.internal.service.DropInServiceRegistry

abstract class DropInService : LifecycleService() {

    override fun onCreate() {
        super.onCreate()
        DropInServiceRegistry.register(this)
    }

    override fun onDestroy() {
        DropInServiceRegistry.unregister()
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    abstract suspend fun onSubmit(state: PaymentComponentState<*>): CheckoutResult

    abstract suspend fun onAdditionalDetails(data: ActionComponentData): CheckoutResult
}
