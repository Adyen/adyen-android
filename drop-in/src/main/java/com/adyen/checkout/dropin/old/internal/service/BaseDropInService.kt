/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RestrictTo
import com.adyen.checkout.card.old.BinLookupData
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.internal.util.bufferedChannel
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.DispatcherProvider
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.old.AddressLookupDropInServiceResult
import com.adyen.checkout.dropin.old.BalanceDropInServiceResult
import com.adyen.checkout.dropin.old.BaseDropInServiceContract
import com.adyen.checkout.dropin.old.BaseDropInServiceResult
import com.adyen.checkout.dropin.old.DropInServiceResult
import com.adyen.checkout.dropin.old.OrderDropInServiceResult
import com.adyen.checkout.dropin.old.RecurringDropInServiceResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import kotlin.coroutines.CoroutineContext

@Suppress("TooManyFunctions")
abstract class BaseDropInService
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
constructor() : Service(), CoroutineScope, BaseDropInServiceInterface, BaseDropInServiceContract {

    private val coroutineJob: Job = Job()
    final override val coroutineContext: CoroutineContext get() = DispatcherProvider.Main + coroutineJob

    @Suppress("LeakingThis")
    private val binder = DropInBinder(this)

    private val resultChannel: Channel<BaseDropInServiceResult> = bufferedChannel()
    private val resultFlow = resultChannel.receiveAsFlow()

    private var additionalData: Bundle? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder {
        adyenLog(AdyenLogLevel.DEBUG) { "onBind" }
        if (intent?.hasExtra(INTENT_EXTRA_ADDITIONAL_DATA) == true) {
            additionalData = intent.getBundleExtra(INTENT_EXTRA_ADDITIONAL_DATA)
        }
        return binder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        adyenLog(AdyenLogLevel.DEBUG) { "onUnbind" }
        return super.onUnbind(intent)
    }

    override fun onRebind(intent: Intent?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onRebind" }
        super.onRebind(intent)
    }

    override fun onCreate() {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
        super.onCreate()
    }

    override fun onDestroy() {
        adyenLog(AdyenLogLevel.DEBUG) { "onDestroy" }

        cancel()

        super.onDestroy()
    }

    final override fun sendResult(result: DropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "dispatching DropInServiceResult" }
        emitResult(result)
    }

    final override fun sendBalanceResult(result: BalanceDropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "dispatching BalanceDropInServiceResult" }
        emitResult(result)
    }

    final override fun sendOrderResult(result: OrderDropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "dispatching OrderDropInServiceResult" }
        emitResult(result)
    }

    final override fun sendRecurringResult(result: RecurringDropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "dispatching RecurringDropInServiceResult" }
        emitResult(result)
    }

    final override fun sendAddressLookupResult(result: AddressLookupDropInServiceResult) {
        adyenLog(AdyenLogLevel.DEBUG) { "dispatching AddressLookupDropInServiceResult" }
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
        adyenLog(AdyenLogLevel.DEBUG) { "requestRemoveStoredPaymentMethod" }
        onRemoveStoredPaymentMethod(storedPaymentMethod)
    }

    final override fun getAdditionalData(): Bundle? {
        return additionalData
    }

    final override fun onRedirectCalled() {
        onRedirect()
    }

    final override fun onBinValueCalled(binValue: String) {
        onBinValue(binValue)
    }

    final override fun onBinLookupCalled(data: List<BinLookupData>) {
        onBinLookup(data)
    }

    final override fun onAddressLookupQueryChangedCalled(query: String) {
        onAddressLookupQueryChanged(query)
    }

    final override fun onAddressLookupCompletionCalled(lookupAddress: LookupAddress): Boolean {
        return onAddressLookupCompletion(lookupAddress)
    }

    internal class DropInBinder(service: BaseDropInService) : Binder() {

        private val serviceRef: WeakReference<BaseDropInService> = WeakReference(service)

        fun getService(): BaseDropInServiceInterface? = serviceRef.get()
    }

    companion object {

        private const val INTENT_EXTRA_ADDITIONAL_DATA = "ADDITIONAL_DATA"

        internal fun bindService(
            context: Context,
            connection: ServiceConnection,
            merchantService: ComponentName,
            additionalData: Bundle?,
        ): Boolean {
            adyenLog(AdyenLogLevel.DEBUG) { "bindService - ${context::class.simpleName}" }
            val intent = Intent().apply {
                component = merchantService
                putExtra(INTENT_EXTRA_ADDITIONAL_DATA, additionalData)
            }
            return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        internal fun unbindService(context: Context, connection: ServiceConnection) {
            adyenLog(AdyenLogLevel.DEBUG) { "unbindService - ${context::class.simpleName}" }
            context.unbindService(connection)
        }
    }
}
