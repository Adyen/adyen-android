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
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.channel.bufferedChannel
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.dropin.DropInConfiguration
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
abstract class BaseDropInService : Service(), CoroutineScope, DropInServiceInterface {

    private val coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + coroutineJob

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

    /**
     * Allow asynchronously sending the results of the payments/ and payments/details/ network
     * calls.
     *
     * Call this method when using [onPaymentsCallRequested] and [onDetailsCallRequested] with a
     * [DropInServiceResult] depending on the response of the corresponding network call.
     * Check the subclasses of [DropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    protected fun sendResult(result: DropInServiceResult) {
        Logger.d(TAG, "dispatching DropInServiceResult")
        emitResult(result)
    }

    /**
     * Allow asynchronously sending the results of the paymentMethods/balance/ network call.
     *
     * Call this method when using [checkBalance] with a [BalanceDropInServiceResult] depending
     * on the response of the corresponding network call.
     * Check the subclasses of [BalanceDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    protected fun sendBalanceResult(result: BalanceDropInServiceResult) {
        Logger.d(TAG, "dispatching BalanceDropInServiceResult")
        emitResult(result)
    }

    /**
     * Allow asynchronously sending the results of the orders/ network call.
     *
     * Call this method when using [createOrder] with a [OrderDropInServiceResult] depending
     * on the response of the corresponding network call.
     * Check the subclasses of [OrderDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    protected fun sendOrderResult(result: OrderDropInServiceResult) {
        Logger.d(TAG, "dispatching OrderDropInServiceResult")
        emitResult(result)
    }

    /**
     * Allow asynchronously sending the results of the Recurring/ network call.
     *
     * Call this method after making a network call to remove a stored payment method
     * while using [removeStoredPaymentMethod] and pass an instance of [RecurringDropInServiceResult]
     * depending on the response of the corresponding network call.
     * Check the subclasses of [RecurringDropInServiceResult] for more information.
     *
     * @param result the result of the network request.
     */
    protected fun sendRecurringResult(result: RecurringDropInServiceResult) {
        Logger.d(TAG, "dispatching RecurringDropInServiceResult")
        emitResult(result)
    }

    protected fun emitResult(result: BaseDropInServiceResult) {
        launch {
            // send response back to activity
            resultChannel.send(result)
        }
    }

    override suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit) {
        resultFlow.collect { callback(it) }
    }

    override fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        Logger.d(TAG, "requestRemoveStoredPaymentMethod")
        removeStoredPaymentMethod(storedPaymentMethod)
    }

    /**
     * Only applicable to removing stored payment methods.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * Recurring/<version_number>/disable endpoint. This method is called when the user initiates
     * removing a stored payment method using the remove button.
     *
     * We provide [storedPaymentMethod] that contains the id of the stored payment method to be removed
     * in the field [StoredPaymentMethod.id].
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * Recurring/<version>/disable call and any other long running operation is made on a background thread.
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    open fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        throw NotImplementedError("Method removeStoredPaymentMethod is not implemented")
    }

    /**
     * Gets the additional data that was set when starting drop-in using
     * [DropInConfiguration.Builder.setAdditionalDataForDropInService] or null if nothing was set.
     */
    protected fun getAdditionalData(): Bundle? {
        return additionalData
    }

    internal class DropInBinder(service: BaseDropInService) : Binder() {

        private val serviceRef: WeakReference<BaseDropInService> = WeakReference(service)

        fun getService(): DropInServiceInterface? = serviceRef.get()
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

internal interface DropInServiceInterface {
    suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit)
    fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>)
    fun requestDetailsCall(actionComponentData: ActionComponentData)
    fun requestBalanceCall(paymentMethodData: PaymentMethodDetails)
    fun requestOrdersCall()
    fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean)
    fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod)
}
