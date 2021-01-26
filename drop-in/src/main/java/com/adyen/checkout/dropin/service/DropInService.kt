/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

private val TAG = LogUtil.getTag()

/**
 * Base service to be extended by the merchant to provide the network calls that connect to the Adyen endpoints.
 * Calls should be made to your server, and from there to Adyen.
 *
 * The methods [makePaymentsCall] and [makeDetailsCall] are already run in the background and can return synchronously.
 * For async, check documentation.
 * The result [DropInServiceResult] is the result of the network call and can mean different things.
 * Check the subclasses of [DropInServiceResult] for more information.
 */
@Suppress("TooManyFunctions")
abstract class DropInService : Service(), CoroutineScope {

    private val coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + coroutineJob

    private val binder = DropInBinder()

    private val resultLiveData: MutableLiveData<DropInServiceResult> = MutableLiveData()

    override fun onBind(intent: Intent?): IBinder {
        Logger.d(TAG, "onBind")
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
        super.onDestroy()
    }

    internal fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        Logger.d(TAG, "requestPaymentsCall")
        val json = PaymentComponentData.SERIALIZER.serialize(paymentComponentState.data)
        onPaymentsCallRequested(paymentComponentState, json)
    }

    protected open fun onPaymentsCallRequested(
        paymentComponentState: PaymentComponentState<*>,
        paymentComponentJson: JSONObject
    ) {
        launch(Dispatchers.IO) {
            // Merchant makes network call
            val result = makePaymentsCall(paymentComponentJson)
            sendResult(result)
        }
    }

    internal fun requestDetailsCall(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "requestDetailsCall")
        val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
        onDetailsCallRequested(actionComponentData, json)
    }

    protected open fun onDetailsCallRequested(
        actionComponentData: ActionComponentData,
        actionComponentJson: JSONObject
    ) {
        launch(Dispatchers.IO) {
            // Merchant makes network call
            val result = makeDetailsCall(actionComponentJson)
            sendResult(result)
        }
    }

    protected fun sendResult(result: DropInServiceResult) {
        // send response back to activity
        Logger.d(TAG, "dispatching DropInServiceResult")
        resultLiveData.postValue(result)
    }

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/ endpoint.
     *
     * We provide a [PaymentComponentData] (as JSONObject) with the parameters we can infer from
     * the Component [Configuration] and the user input,
     * specially the "paymentMethod" object with the shopper input details.
     * The rest of the payments/ call object should be filled in, on your server, according to your needs.
     *
     * You can use [PaymentComponentData.SERIALIZER] to serialize the data between the data
     * object and a [JSONObject] depending on what you prefer.
     *
     * The return of this method is expected to be a [DropInServiceResult] with the result of the network
     * request.
     * See expected [DropInServiceResult] and the associated content.
     *
     * This call is expected to be synchronous, as it already runs in a background thread, and the
     * base class will handle messaging the UI after it finishes, based on the [DropInServiceResult].
     * If you want to make the call asynchronously, return [DropInServiceResult.Wait] on the type and
     * call the [asyncCallback] method afterwards when it is done with the result.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentJson The result data from the [PaymentComponent] the compose your call.
     * @return The result of the network call
     */
    open fun makePaymentsCall(paymentComponentJson: JSONObject): DropInServiceResult {
        throw NotImplementedError("Neither makePaymentsCall nor onPaymentsCallRequested is implemented")
    }

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/details/ endpoint.
     *
     * We provide a [ActionComponentData] (as JSONObject) with the whole result expected by the payments/details/ endpoint
     * (if paymentData was provided).
     *
     * You can use [ActionComponentData.SERIALIZER] to serialize the data between the data object
     * and a [JSONObject] depending on what you prefer.
     *
     * This call is expected to be synchronous, as it already runs in the background, and the base class will handle messaging with the UI after it
     * finishes based on the [DropInServiceResult]. If you want to make the call asynchronously, return
     * [DropInServiceResult.Wait] on the type and call the [asyncCallback] method afterwards.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentJson The result data from the [ActionComponent] the compose your call.
     * @return The result of the network call
     */
    open fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        throw NotImplementedError("Neither makeDetailsCall nor onDetailsCallRequested is implemented")
    }

    internal fun observeResult(owner: LifecycleOwner, observer: Observer<DropInServiceResult>) {
        this.resultLiveData.observe(owner, observer)
    }

    inner class DropInBinder : Binder() {
        fun getService(): DropInService = this@DropInService
    }

    companion object {
        internal fun bindService(
            context: Context,
            connection: ServiceConnection,
            merchantService: ComponentName
        ): Boolean {
            Logger.d(TAG, "bindService - ${context::class.simpleName}")
            val intent = Intent().apply { component = merchantService }
            return context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        internal fun unbindService(context: Context, connection: ServiceConnection) {
            Logger.d(TAG, "unbindService - ${context::class.simpleName}")
            context.unbindService(connection)
        }
    }
}
