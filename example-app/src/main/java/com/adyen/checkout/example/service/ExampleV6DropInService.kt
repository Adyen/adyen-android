/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/1/2026.
 */

package com.adyen.checkout.example.service

import android.util.Log
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.AdditionalDetailsResult
import com.adyen.checkout.core.components.SubmitResult
import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.error.CheckoutError
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.extensions.toStringPretty
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.old.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ExampleV6DropInService : DropInService() {

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override suspend fun onSubmit(data: PaymentComponentData<*>): SubmitResult {
        val paymentComponentJson = PaymentComponentData.SERIALIZER.serialize(data)
        // Check out the documentation of this method on the parent DropInService class
        val paymentRequest = createPaymentRequest(
            paymentComponentData = paymentComponentJson,
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getOldAmount(),
            countryCode = keyValueStorage.getCountry(),
            merchantAccount = keyValueStorage.getMerchantAccount(),
            // TODO - Move return url creation to the new redirect component
            redirectUrl = RedirectComponent.getReturnUrl(applicationContext),
            threeDSMode = keyValueStorage.getThreeDSMode(),
            shopperEmail = keyValueStorage.getShopperEmail(),
        )

        Log.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
        val response = paymentsRepository.makePaymentsRequest(paymentRequest)
        return handleSubmitResponse(response)
    }

    override suspend fun onAdditionalDetails(data: ActionComponentData): AdditionalDetailsResult {
        Log.d(TAG, "onAdditionalDetails")
        val actionComponentJson = ActionComponentData.SERIALIZER.serialize(data)
        Log.v(TAG, "actionComponentJson - ${actionComponentJson.toStringPretty()}")
        val response = paymentsRepository.makeDetailsRequest(actionComponentJson)
        return handleAdditionalDetailsResponse(response)
    }

    private fun handleSubmitResponse(jsonResponse: JSONObject?): SubmitResult {
        return when {
            jsonResponse == null -> {
                Log.e(TAG, "FAILED")
                SubmitResult.Error(
                    CheckoutError(
                        code = CheckoutError.ErrorCode.UNKNOWN,
                        message = "IOException",
                    )
                )
            }

            isAction(jsonResponse) -> {
                Log.d(TAG, "Received action")
                val action = Action.SERIALIZER.deserialize(jsonResponse.getJSONObject("action"))
                SubmitResult.Action(action)
            }

            else -> {
                Log.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
                val resultCode = if (jsonResponse.has("resultCode")) {
                    jsonResponse.get("resultCode").toString()
                } else {
                    "EMPTY"
                }
                SubmitResult.Finished(resultCode)
            }
        }
    }

    private fun handleAdditionalDetailsResponse(jsonResponse: JSONObject?): AdditionalDetailsResult {
        return when {
            jsonResponse == null -> {
                Log.e(TAG, "FAILED")
                AdditionalDetailsResult.Error(
                    CheckoutError(
                        code = CheckoutError.ErrorCode.UNKNOWN,
                        message = "IOException",
                    )
                )
            }

            else -> {
                Log.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
                val resultCode = jsonResponse.optString("resultCode").takeIf { it.isNotEmpty() }
                AdditionalDetailsResult.Finished(resultCode)
            }
        }
    }

    private fun isAction(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("action")
    }

    companion object {
        private val TAG = getLogTag()
    }
}
