/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/3/2019.
 */

package com.adyen.checkout.example

import com.adyen.checkout.base.model.payments.request.CardPaymentMethod
import com.adyen.checkout.base.model.payments.request.DotpayPaymentMethod
import com.adyen.checkout.base.model.payments.request.EPSPaymentMethod
import com.adyen.checkout.base.model.payments.request.EntercashPaymentMethod
import com.adyen.checkout.base.model.payments.request.GenericPaymentMethod
import com.adyen.checkout.base.model.payments.request.IdealPaymentMethod
import com.adyen.checkout.base.model.payments.request.MolpayPaymentMethod
import com.adyen.checkout.base.model.payments.request.OpenBankingPaymentMethod
import com.adyen.checkout.base.model.payments.request.PaymentComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.api.CallResult
import com.adyen.checkout.example.api.CheckoutApiService
import com.adyen.checkout.example.api.model.PaymentsRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.IOException

/**
 * This is just an example on how to make network calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 */
class ExampleDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makePaymentsCall")

        val paymentsRequest = PaymentsRequest(PaymentComponentData.SERIALIZER.deserialize(paymentComponentData))

        val moshi = Moshi.Builder()
                .add(PolymorphicJsonAdapterFactory.of(PaymentComponentData::class.java, PaymentComponentData.TYPE)
                        .withSubtype(CardPaymentMethod::class.java, CardPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(IdealPaymentMethod::class.java, IdealPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(MolpayPaymentMethod::class.java, MolpayPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(EPSPaymentMethod::class.java, EPSPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(DotpayPaymentMethod::class.java, DotpayPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(EntercashPaymentMethod::class.java, EntercashPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(OpenBankingPaymentMethod::class.java, OpenBankingPaymentMethod.PAYMENT_METHOD_TYPE)
                        .withSubtype(GenericPaymentMethod::class.java, "other")
                )
                .build()
        val jsonAdapter = moshi.adapter(PaymentsRequest::class.java)
        val requestString = jsonAdapter.toJson(paymentsRequest)
        Logger.v(TAG, "requestJson - ${JSONObject(requestString).toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = RequestBody.create(MediaType.parse("application/json"), requestString)

        val call = CheckoutApiService.INSTANCE.payments(requestBody)

        return try {
            val response = call.execute()
            val paymentsResponse = response.body()

            // Error body
            val byteArray = response.errorBody()?.bytes()
            if (byteArray != null) {
                Logger.e(TAG, "errorBody - ${String(byteArray)}")
            }

            if (response.isSuccessful && paymentsResponse != null) {

                if (paymentsResponse.action != null) {
                    CallResult(CallResult.ResultType.ACTION, Action.SERIALIZER.serialize(paymentsResponse.action).toString())
                } else {
                    CallResult(CallResult.ResultType.FINISHED, paymentsResponse.resultCode ?: "EMPTY")
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                CallResult(CallResult.ResultType.ERROR, "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            CallResult(CallResult.ResultType.ERROR, "IOException")
        }
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        Logger.d(TAG, "makeDetailsCall \n${actionComponentData.toString(JsonUtils.IDENT_SPACES)}")

        val requestBody = RequestBody.create(MediaType.parse("application/json"), actionComponentData.toString())
        val call = CheckoutApiService.INSTANCE.details(requestBody)

        return try {
            val response = call.execute()
            val detailsResponse = response.body()

            if (response.isSuccessful && detailsResponse != null) {
                if (detailsResponse.action != null) {
                    CallResult(CallResult.ResultType.ACTION, Action.SERIALIZER.serialize(detailsResponse.action).toString())
                } else {
                    CallResult(CallResult.ResultType.FINISHED, detailsResponse.resultCode ?: "EMPTY")
                }
            } else {
                Logger.e(TAG, "FAILED - ${response.message()}")
                CallResult(CallResult.ResultType.ERROR, "IOException")
            }
        } catch (e: IOException) {
            Logger.e(TAG, "IOException", e)
            CallResult(CallResult.ResultType.ERROR, "IOException")
        }
    }
}
