/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 19/1/2026.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.dropin.internal.service.DropInInteractor
import java.lang.ref.WeakReference

abstract class DropInService : LifecycleService(), DropInInteractor {

    private val binder by lazy { DropInBinder(this) }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)
        return binder
    }

    abstract override suspend fun onSubmit(): CheckoutResult

    internal class DropInBinder(service: DropInService) : Binder() {

        private val serviceRef: WeakReference<DropInService> = WeakReference(service)

        fun getService(): DropInInteractor? = serviceRef.get()
    }
}
