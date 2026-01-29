/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/1/2026.
 */

package com.adyen.checkout.dropin.internal.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState
import com.adyen.checkout.dropin.DropInService

internal class DropInServiceManager(
    private val serviceClass: Class<out DropInService>,
) {

    private var binder: DropInBinder? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val localBinder = service as? DropInBinder
            if (localBinder != null) {
                binder = localBinder
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            binder = null
        }
    }

    fun startAndBind(context: Context) {
        val intent = Intent(context, serviceClass)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    fun unbind(context: Context) {
        try {
            context.unbindService(connection)
        } catch (e: IllegalArgumentException) {
            adyenLog(AdyenLogLevel.WARN, e) { "Failed to unbind service" }
        }
        binder = null
    }

    fun stop(context: Context) {
        val intent = Intent(context, serviceClass)
        context.stopService(intent)
    }

    // TODO - Binder could be null when service is not connected yet.
    //  Implement a queue similar in functionality to one in v5 DropInActivity.
    suspend fun requestOnSubmit(state: PaymentComponentState<*>): CheckoutResult {
        return binder?.requestOnSubmit(state) ?: error("Service is not bound")
    }
}
