/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/1/2023.
 */

package com.adyen.checkout.dropin.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RestrictTo
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
abstract class BaseDropInService : Service(), CoroutineScope, BaseDropInServiceInterface, BaseDropInServiceContract {

    private val coroutineJob: Job = Job()
    final override val coroutineContext: CoroutineContext get() = Dispatchers.Main + coroutineJob

    @Suppress("LeakingThis")
    private val binder = DropInBinder(this)

    private val resultChannel: Channel<BaseDropInServiceResult> = bufferedChannel()
    private val resultFlow = resultChannel.receiveAsFlow()

    private var additionalData: Bundle? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        Logger.d(TAG, "onBind")
        if (intent?.hasExtra(INTENT_EXTRA_ADDITIONAL_DATA) == true) {
            additionalData = intent.getBundleExtra(INTENT_EXTRA_ADDITIONAL_DATA)
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Logger.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        Logger.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    override fun onCreate() {
        Logger.d(TAG, "onCreate")
        super.onCreate()
    }

    override fun onDestroy() {
        Logger.d(TAG, "onDestroy")

        cancel()

        super.onDestroy()
    }

    final override fun sendResult(result: DropInServiceResult) {
        Logger.d(TAG, "dispatching DropInServiceResult")
        emitResult(result)
    }

    final override fun sendBalanceResult(result: BalanceDropInServiceResult) {
        Logger.d(TAG, "dispatching BalanceDropInServiceResult")
        emitResult(result)
    }

    final override fun sendOrderResult(result: OrderDropInServiceResult) {
        Logger.d(TAG, "dispatching OrderDropInServiceResult")
        emitResult(result)
    }

    final override fun sendRecurringResult(result: RecurringDropInServiceResult) {
        Logger.d(TAG, "dispatching RecurringDropInServiceResult")
        emitResult(result)
    }

    protected fun emitResult(result: BaseDropInServiceResult) {
        launch {
            // send response back to activity
            resultChannel.send(result)
        }
    }

    final override suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit) {
        resultFlow.collect { callback(it) }
    }

    final override fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        Logger.d(TAG, "requestRemoveStoredPaymentMethod")
        onRemoveStoredPaymentMethod(storedPaymentMethod)
    }

    final override fun getAdditionalData(): Bundle? {
        return additionalData
    }

    internal class DropInBinder(service: BaseDropInService) : Binder() {

        private val serviceRef: WeakReference<BaseDropInService> = WeakReference(service)

        fun getService(): BaseDropInServiceInterface? = serviceRef.get()
    }

    companion object {

        private val TAG = LogUtil.getTag()

        private const val INTENT_EXTRA_ADDITIONAL_DATA = "ADDITIONAL_DATA"

        internal fun startService(
            context: Context,
            connection: ServiceConnection,
            merchantService: ComponentName,
            additionalData: Bundle?,
        ): Boolean {
            Logger.d(TAG, "startService - ${context::class.simpleName}")
            val intent = Intent().apply {
                component = merchantService
            }
            Logger.d(TAG, "merchant service: ${merchantService.className}")
            context.startService(intent)
            return bindService(context, connection, merchantService, additionalData)
        }

        private fun bindService(
            context: Context,
            connection: ServiceConnection,
            merchantService: ComponentName,
            additionalData: Bundle?,
        ): Boolean {
            Logger.d(TAG, "bindService - ${context::class.simpleName}")
            val intent = Intent().apply {
                component = merchantService
                putExtra(INTENT_EXTRA_ADDITIONAL_DATA, additionalData)
            }
            return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        internal fun stopService(
            context: Context,
            merchantService: ComponentName,
            connection: ServiceConnection,
        ) {
            unbindService(context, connection)

            Logger.d(TAG, "stopService - ${context::class.simpleName}")

            val intent = Intent().apply {
                component = merchantService
            }
            context.stopService(intent)
        }

        private fun unbindService(context: Context, connection: ServiceConnection) {
            Logger.d(TAG, "unbindService - ${context::class.simpleName}")
            context.unbindService(connection)
        }
    }
}
