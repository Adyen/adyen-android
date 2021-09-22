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
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject

private val TAG = LogUtil.getTag()

/**
 * Base service to be extended by the merchant to provide the network calls that connect to the Adyen endpoints.
 * Calls should be made to your server, and from there to Adyen.
 *
 * The methods [makePaymentsCall] and [makeDetailsCall] are already run in the background and can return synchronously.
 * For async, you can override [onPaymentsCallRequested] and [onDetailsCallRequested] instead.
 * Check the documentation for more details.
 * The result [DropInServiceResult] is the result of the network call and can mean different things.
 * Check the subclasses of [DropInServiceResult] for more information.
 */
@Suppress("TooManyFunctions")
abstract class DropInService : Service(), CoroutineScope, DropInServiceInterface {

    private val coroutineJob: Job = Job()
    override val coroutineContext: CoroutineContext get() = Dispatchers.Main + coroutineJob

    private val binder = DropInBinder()

    private val resultLiveData: MutableLiveData<DropInServiceResult> = MutableLiveData()
    private val balanceLiveData: MutableLiveData<BalanceResult> = MutableLiveData()

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

    override fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>) {
        Logger.d(TAG, "requestPaymentsCall")
        val json = PaymentComponentData.SERIALIZER.serialize(paymentComponentState.data)
        onPaymentsCallRequested(paymentComponentState, json)
    }

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/ endpoint.
     *
     * We provide a [PaymentComponentData] (as JSONObject) with the parameters we can infer from
     * the Component [Configuration] and the user input,
     * specially the "paymentMethod" object with the shopper input details.
     * The rest of the payments/ call object should be filled in, on your server, according to your needs.
     *
     * We also provide a [PaymentComponentState] that contains a non-serialized version of the
     * payment component JSON and might also contain more details about the state of the
     * component at the moment in which the payment is confirmed by the user.
     *
     * - Asynchronous handling:
     *
     *     Since this method runs on the main thread, you should make sure the payments/ call and
     * any other long running operation is made on a background thread. You should eventually call
     * [sendResult] with a [DropInServiceResult] containing the result of the network request.
     * The base class will handle messaging the UI afterwards, based on the [DropInServiceResult].
     *
     *     Note that overriding this method means that the [makePaymentsCall] method will not be
     * called anymore and therefore you can disregard it.
     *
     * - Synchronous handling:
     *
     *     Alternatively, if you don't need asynchronous handling but you still want to access
     * the [PaymentComponentState], you will still need to implement [makePaymentsCall]. After you
     * are done handling the [PaymentComponentState] inside [onPaymentsCallRequested], call
     * [super.onPaymentsCallRequested] to proceed. This will internally invoke your
     * implementation of [makePaymentsCall] in a background thread so you won't need to
     * manage the threads yourself.
     *
     * Note that the [PaymentComponentState] is a abstract class, you can check and cast to
     * one of its child classes for a more component specific state.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentState The state of the [PaymentComponent] at the moment the user
     * submits the payment.
     * @param paymentComponentJson The serialized data from the [PaymentComponent] the compose your
     * call.
     */
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

    override fun requestDetailsCall(actionComponentData: ActionComponentData) {
        Logger.d(TAG, "requestDetailsCall")
        val json = ActionComponentData.SERIALIZER.serialize(actionComponentData)
        onDetailsCallRequested(actionComponentData, json)
    }

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/details/ endpoint.
     *
     * We provide an [ActionComponentData] (as JSONObject) with the whole result expected by the
     * payments/details/ endpoint (if paymentData was provided).
     *
     * We also provide an [ActionComponentData] that contains a non-serialized version of the
     * action component JSON.
     *
     * - Asynchronous handling:
     *
     *     Since this method runs on the main thread, you should make sure the payments/details/
     * call and any other long running operation is made on a background thread. You should
     * eventually call [sendResult] with a [DropInServiceResult] containing the result of the
     * network request. The base class will handle messaging the UI afterwards, based on the
     * [DropInServiceResult].
     *
     *     Note that overriding this method means that the [makeDetailsCall] method will not be
     * called anymore and therefore you can disregard it.
     *
     * - Synchronous handling:
     *
     *     Alternatively, if you don't need asynchronous handling but you still want to access
     * the [ActionComponentData], you will still need to implement [makeDetailsCall]. After you
     * are done handling the [ActionComponentData] inside [onDetailsCallRequested], call
     * [super.onDetailsCallRequested] to proceed. This will internally invoke your
     * implementation of [makeDetailsCall] in a background thread so you won't need to
     * manage the threads yourself.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The data from the [ActionComponent].
     * @param actionComponentJson The serialized data from the [ActionComponent] the compose your
     * call.
     */
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
     * request. Check the subclasses of [DropInServiceResult] for more information.
     *
     * This call is expected to be synchronous, as it already runs in a background thread, and the
     * base class will handle messaging the UI after it finishes, based on the
     * [DropInServiceResult].
     *
     * If you want to make the call asynchronously, or get a more detailed, non-serialized
     * version of the payment component data, override [onPaymentsCallRequested] instead.
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
     * We provide an [ActionComponentData] (as JSONObject) with the whole result expected by the
     * payments/details/ endpoint (if paymentData was provided).
     *
     * You can use [ActionComponentData.SERIALIZER] to serialize the data between the data object
     * and a [JSONObject] depending on what you prefer.
     *
     * The return of this method is expected to be a [DropInServiceResult] with the result of the network
     * request. Check the subclasses of [DropInServiceResult] for more information.
     *
     * This call is expected to be synchronous, as it already runs in the background, and the
     * base class will handle messaging with the UI after it finishes based on the
     * [DropInServiceResult].
     *
     * If you want to make the call asynchronously, or get a non-serialized version of the action
     * component data, override [onDetailsCallRequested] instead.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentJson The result data from the [ActionComponent] the compose your call.
     * @return The result of the network call
     */
    open fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        throw NotImplementedError("Neither makeDetailsCall nor onDetailsCallRequested is implemented")
    }

    override fun requestBalanceCall(paymentMethodData: PaymentMethodDetails) {
        Logger.d(TAG, "requestBalanceCall")
        val json = PaymentMethodDetails.SERIALIZER.serialize(paymentMethodData)
        checkBalance(paymentMethodData, json)
    }

    // TODO docs
    open fun checkBalance(paymentMethodData: PaymentMethodDetails, paymentMethodJson: JSONObject) {
        throw NotImplementedError("Method checkBalance is not implemented")
    }

    // TODO docs
    protected fun onBalanceChecked(balanceJson: String, transactionLimitJson: String?) {
        // send response back to activity
        Logger.d(TAG, "onBalanceChecked called")
        balanceLiveData.postValue(BalanceResult(balanceJson, transactionLimitJson))
    }

    override fun observeResult(owner: LifecycleOwner, observer: Observer<DropInServiceResult>) {
        resultLiveData.observe(owner, observer)
    }

    override fun observeBalanceResult(owner: LifecycleOwner, observer: Observer<BalanceResult>) {
        balanceLiveData.observe(owner, observer)
    }

    internal inner class DropInBinder : Binder() {
        fun getService(): DropInServiceInterface = this@DropInService
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

internal interface DropInServiceInterface {
    fun observeResult(owner: LifecycleOwner, observer: Observer<DropInServiceResult>)
    fun requestPaymentsCall(paymentComponentState: PaymentComponentState<*>)
    fun requestDetailsCall(actionComponentData: ActionComponentData)
    fun observeBalanceResult(owner: LifecycleOwner, observer: Observer<BalanceResult>)
    fun requestBalanceCall(paymentMethodData: PaymentMethodDetails)
}
