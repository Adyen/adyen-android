/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 2/7/2019.
 */

package com.adyen.checkout.dropin.service

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.JobIntentService
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import org.json.JSONObject

/**
 * Base service to be extended by the merchant to provide the network calls that connect to the Adyen endpoints.
 * Calls should be made to your server, and from there to Adyen.
 *
 * The methods [makePaymentsCall] and [makeDetailsCall] are already run in the background and can return synchronously. Or async, check documentation.
 * The result [CallResult] is the result of the network call and can mean different things. Check the [CallResult.ResultType] for more information.
 */
abstract class DropInService : JobIntentService() {

    companion object {
        protected val TAG = LogUtil.getTag()

        // Key of the response content on the intent bundle
        const val API_CALL_RESULT_KEY = "payments_api_call_result"

        // Define the type of request the service needs to perform
        private const val REQUEST_TYPE_KEY = "request_type"
        private const val PAYMENTS_REQUEST = "type_payments"
        private const val DETAILS_REQUEST = "type_details"

        // Internal key of the content for the request
        private const val PAYMENT_COMPONENT_DATA_EXTRA_KEY = "payment_component_data_extra"
        private const val DETAILS_EXTRA_KEY = "details_method_extra"

        // Make it public for merchants who want to override behavior
        @Suppress("MemberVisibilityCanBePrivate")
        const val dropInJobId = 11

        // Base for the action strings
        private const val adyenCheckoutBaseActionSuffix = ".adyen.checkout"
        // com.merchant.package.adyen.checkout.CALL_RESULT
        private const val callResultSuffix = "$adyenCheckoutBaseActionSuffix.CALL_RESULT"

        /**
         * Get the action sent to the [LocalBroadcastManager] to notify the result of the payments call.
         */
        fun getServiceResultAction(context: Context): String {
            return context.packageName + callResultSuffix
        }

        /**
         * Helper function that sends a request for the merchant to make the payments call.
         */
        // False positive
        @Suppress("FunctionParameterNaming")
        fun requestPaymentsCall(
            context: Context,
            paymentComponentData: PaymentComponentData<out PaymentMethodDetails>,
            merchantService: ComponentName
        ) {
            Logger.d(TAG, "requestPaymentsCall - ${paymentComponentData.paymentMethod?.type}")

            val workIntent = Intent()
            workIntent.putExtra(REQUEST_TYPE_KEY, PAYMENTS_REQUEST)
            workIntent.putExtra(PAYMENT_COMPONENT_DATA_EXTRA_KEY, paymentComponentData)

            enqueueWork(context, merchantService, dropInJobId, workIntent)
        }

        /**
         * Helper function that sends a request for the merchant to make the details call.
         */
        fun requestDetailsCall(context: Context, details: JSONObject, merchantService: ComponentName) {
            Logger.d(TAG, "requestDetailsCall")

            val workIntent = Intent()
            workIntent.putExtra(REQUEST_TYPE_KEY, DETAILS_REQUEST)
            workIntent.putExtra(DETAILS_EXTRA_KEY, details.toString())

            enqueueWork(context, merchantService, dropInJobId, workIntent)
        }
    }

    override fun onHandleWork(intent: Intent) {
        Logger.d(TAG, "onHandleWork")

        when (intent.getStringExtra(REQUEST_TYPE_KEY)) {
            PAYMENTS_REQUEST -> {
                val paymentComponentDataForRequest =
                    intent.getParcelableExtra<PaymentComponentData<in PaymentMethodDetails>>(PAYMENT_COMPONENT_DATA_EXTRA_KEY)
                askPaymentsCall(paymentComponentDataForRequest)
            }
            DETAILS_REQUEST -> {
                val detailsString = intent.getStringExtra(DETAILS_EXTRA_KEY)
                val details = JSONObject(detailsString)
                askDetailsCall(details)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Android will only call new tasks from enqueueWork if the previous one has finished nas the service is destroyed
        Logger.d(TAG, "onDestroy")
    }

    /**
     * Call this method for asynchronous handling of [makePaymentsCall]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    protected fun asyncCallback(callResult: CallResult) {
        handleCallResult(callResult)
    }

    private fun askPaymentsCall(paymentComponentData: PaymentComponentData<in PaymentMethodDetails>) {
        Logger.d(TAG, "askPaymentsCall")

        // Merchant makes network call
        val paymentsCallResult = makePaymentsCall(PaymentComponentData.SERIALIZER.serialize(paymentComponentData))

        handleCallResult(paymentsCallResult)
    }

    private fun askDetailsCall(details: JSONObject) {
        Logger.d(TAG, "askDetailsCall")

        // Merchant makes network call
        val detailsCallResult = makeDetailsCall(details)

        handleCallResult(detailsCallResult)
    }

    private fun handleCallResult(callResult: CallResult?) {
        if (callResult == null) {
            // Make sure people don't return Null from Java code
            throw CheckoutException("CallResult result from DropInService cannot be null.")
        }
        Logger.d(TAG, "handleCallResult - ${callResult.type.name}")

        // if type is WAIT do nothing and wait for async callback.
        if (callResult.type != CallResult.ResultType.WAIT) {
            // send response back to activity
            val resultIntent = Intent()

            resultIntent.action = getServiceResultAction(this)
            resultIntent.putExtra(API_CALL_RESULT_KEY, callResult)

            val localBroadcastManager = androidx.localbroadcastmanager.content.LocalBroadcastManager.getInstance(this)
            localBroadcastManager.sendBroadcast(resultIntent)
        }
    }

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/ endpoint.
     *
     * We provide a [PaymentComponentData] (as JSONObject) with the parameters we can infer from the Component [Configuration] and the user input,
     * specially the "paymentMethod" object with the shopper input details.
     * The rest of the payments/ call object should be filled in, on your server, according to your needs.
     *
     * You can use [PaymentComponentData.SERIALIZER] to serialize the data between the data object and a [JSONObject] depending on what you prefer.
     *
     * The return of this method is expected to be a [CallResult] with the result of the network request.
     * See expected [CallResult.ResultType] and the associated content.
     *
     * This call is expected to be synchronous, as it already runs in a background thread, and the base class will handle messaging the UI
     * after it finishes, based on the [CallResult]. If you want to make the call asynchronously, return [CallResult.ResultType.WAIT] on the type
     * and call the [asyncCallback] method afterwards when it is done with the result.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param paymentComponentData The result data from the [PaymentComponent] the compose your call.
     * @return The result of the network call
     */
    abstract fun makePaymentsCall(paymentComponentData: JSONObject): CallResult

    /**
     * In this method you should make the network call to tell your server to make a call to the payments/details/ endpoint.
     *
     * We provide a [ActionComponentData] (as JSONObject) with the whole result expected by the payments/details/ endpoint
     * (if paymentData was provided).
     *
     * You can use [ActionComponentData.SERIALIZER] to serialize the data between the data object and a [JSONObject] depending on what you prefer.
     *
     * This call is expected to be synchronous, as it already runs in the background, and the base class will handle messaging with the UI after it
     * finishes based on the [CallResult]. If you want to make the call asynchronously, return [CallResult.ResultType.WAIT] on the type and call the
     * [asyncCallback] method afterwards.
     *
     * See https://docs.adyen.com/api-explorer/ for more information on the API documentation.
     *
     * @param actionComponentData The result data from the [ActionComponent] the compose your call.
     * @return The result of the network call
     */
    abstract fun makeDetailsCall(actionComponentData: JSONObject): CallResult
}
