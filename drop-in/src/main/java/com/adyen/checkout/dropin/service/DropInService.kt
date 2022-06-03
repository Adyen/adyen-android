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
import android.os.Bundle
import android.os.IBinder
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.paymentmethods.StoredPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentComponentData
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
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

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

    private val resultChannel = Channel<BaseDropInServiceResult>(Channel.BUFFERED)
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
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by calling [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentState The state of the [PaymentComponent] at the moment the user
     * submits the payment.
     * @param paymentComponentJson The serialized data from the [PaymentComponent] to compose your
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
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by calling [sendResult] with [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The data from the [ActionComponent].
     * @param actionComponentJson The serialized data from the [ActionComponent] to compose your
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

    internal fun emitResult(result: BaseDropInServiceResult) {
        launch {
            // send response back to activity
            resultChannel.send(result)
        }
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
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by returning [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentJson The result data from the [PaymentComponent] to compose your call.
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
     * Only applicable for gift card flow: in case of a partial payment, you should update Drop-in
     * by returning [DropInServiceResult.Update].
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentJson The result data from the [ActionComponent] to compose your call.
     * @return The result of the network call
     */
    open fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        throw NotImplementedError("Neither makeDetailsCall nor onDetailsCallRequested is implemented")
    }

    override fun requestBalanceCall(paymentMethodData: PaymentMethodDetails) {
        Logger.d(TAG, "requestBalanceCall")
        checkBalance(paymentMethodData)
    }

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * paymentMethods/balance/ endpoint. This method is called right after the user enters their gift card
     * details and submits them.
     *
     * We provide [paymentMethodData], a [PaymentMethodDetails] object that contains a non-serialized
     * version of the gift card payment method JSON. Use [PaymentMethodDetails.SERIALIZER] to serialize it
     * to a [JSONObject].
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * paymentMethods/balance/ call and any other long running operation is made on a background thread.
     *
     * You should eventually call [sendBalanceResult] with a [BalanceDropInServiceResult] containing the result
     * of the network request. The base class will handle messaging the UI afterwards, based on the
     * [BalanceDropInServiceResult].
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentMethodData The data from the gift card component.
     */
    open fun checkBalance(paymentMethodData: PaymentMethodDetails) {
        throw NotImplementedError("Method checkBalance is not implemented")
    }

    override fun requestOrdersCall() {
        Logger.d(TAG, "requestOrdersCall")
        createOrder()
    }

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * orders/ endpoint. This method is called when the user is trying to pay a part of the Drop-in amount
     * using a gift card.
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the orders/
     * call and any other long running operation is made on a background thread.
     *
     * You should eventually call [sendOrderResult] with a [OrderDropInServiceResult] containing the result of the
     * network request. The base class will handle messaging the UI afterwards, based on the [OrderDropInServiceResult].
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     */
    open fun createOrder() {
        throw NotImplementedError("Method createOrder is not implemented")
    }

    override fun requestCancelOrder(order: OrderRequest, isDropInCancelledByUser: Boolean) {
        Logger.d(TAG, "requestCancelOrder")
        cancelOrder(order, !isDropInCancelledByUser)
    }

    /**
     * Only applicable for gift card flow.
     *
     * In this method you should make the network call to tell your server to make a call to the
     * orders/cancel/ endpoint. This method is called during a partial payment, when the user removes
     * their already paid gift cards either by using the remove button or cancelling Drop-in.
     *
     * We provide [order], an [OrderRequest] object that contains a non-serialized version of the order
     * to be cancelled. Use [OrderRequest.SERIALIZER] to serialize it to a [JSONObject].
     *
     * The [shouldUpdatePaymentMethods] flag indicates the next step you should take after the API call
     * is made:
     * - [true] means that Drop-in is still showing and you should therefore call paymentMethods/
     * then update Drop-in with the new list of payment methods, by passing [DropInServiceResult.Update] to
     * [sendResult].
     * - [false] means that Drop-in is being dismissed by the user so there is no need to do any further calls.
     *
     * Asynchronous handling: since this method runs on the main thread, you should make sure the
     * paymentMethods/balance/ call and any other long running operation is made on a background thread.
     *
     * Note that not overriding this method while enabling gift card payments will cause a [NotImplementedError]
     * to be thrown.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param order The data from order being cancelled.
     * @param shouldUpdatePaymentMethods indicates whether payment methods should be re-fetched and passed to Drop-in.
     */
    open fun cancelOrder(order: OrderRequest, shouldUpdatePaymentMethods: Boolean) {
        throw NotImplementedError("Method cancelOrder is not implemented")
    }

    override fun requestRemoveStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod) {
        Logger.d(TAG, "requestRemoveStoredPaymentMethod")
        removeStoredPaymentMethod(storedPaymentMethod, StoredPaymentMethod.SERIALIZER.serialize(storedPaymentMethod))
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
    open fun removeStoredPaymentMethod(storedPaymentMethod: StoredPaymentMethod, storedPaymentMethodJson: JSONObject) {
        throw NotImplementedError("Method removeStoredPaymentMethod is not implemented")
    }

    override suspend fun observeResult(callback: (BaseDropInServiceResult) -> Unit) {
        resultFlow.collect { callback(it) }
    }

    /**
     * Gets the additional data that was set when starting drop-in using
     * [DropInConfiguration.Builder.setAdditionalDataForDropInService] or null if nothing was set.
     */
    protected fun getAdditionalData(): Bundle? {
        return additionalData
    }

    internal inner class DropInBinder : Binder() {
        fun getService(): DropInServiceInterface = this@DropInService
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
