/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/5/2022.
 */

package com.adyen.checkout.example.service

import android.util.Log
import com.adyen.checkout.blik.BlikComponentState
import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.core.DispatcherProvider
import com.adyen.checkout.dropin.DropInServiceResult
import com.adyen.checkout.dropin.ErrorDialog
import com.adyen.checkout.dropin.SessionDropInService
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.extensions.toStringPretty
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ExampleSessionsDropInService : SessionDropInService() {

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    @Suppress("RestrictedApi")
    override fun onSubmit(
        state: PaymentComponentState<*>,
    ): Boolean {
        return if (
            state is BlikComponentState ||
            state is CardComponentState
        ) {
            launch(DispatcherProvider.IO) {
                Log.d(TAG, "onPaymentsCallRequested")

                // Check out the documentation of this method on the parent DropInService class
                val paymentComponentJson = PaymentComponentData.SERIALIZER.serialize(state.data)
                val paymentRequest = createPaymentRequest(
                    paymentComponentData = paymentComponentJson,
                    shopperReference = keyValueStorage.getShopperReference(),
                    amount = keyValueStorage.getAmount(),
                    countryCode = keyValueStorage.getCountry(),
                    merchantAccount = keyValueStorage.getMerchantAccount(),
                    redirectUrl = RedirectComponent.getReturnUrl(applicationContext),
                    threeDSMode = keyValueStorage.getThreeDSMode(),
                    shopperEmail = keyValueStorage.getShopperEmail(),
                )

                Log.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
                val response = paymentsRepository.makePaymentsRequest(paymentRequest)

                val result = handleResponse(response)
                sendResult(result)
            }
            true
        } else {
            false
        }
    }

    @Suppress("RestrictedApi")
    override fun onAdditionalDetails(
        actionComponentData: ActionComponentData,
    ): Boolean {
        return if (isFlowTakenOver) {
            launch(DispatcherProvider.IO) {
                Log.d(TAG, "onDetailsCallRequested")

                val response = paymentsRepository.makeDetailsRequest(
                    ActionComponentData.SERIALIZER.serialize(actionComponentData),
                )

                val result = handleResponse(response)
                sendResult(result)
            }
            true
        } else {
            false
        }
    }

    private fun handleResponse(jsonResponse: JSONObject?): DropInServiceResult {
        return when {
            jsonResponse == null -> {
                Log.e(TAG, "FAILED")
                DropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
            }

            isAction(jsonResponse) -> {
                Log.d(TAG, "Received action")
                val action = Action.SERIALIZER.deserialize(jsonResponse.getJSONObject("action"))
                DropInServiceResult.Action(action)
            }

            else -> {
                Log.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
                val resultCode = if (jsonResponse.has("resultCode")) {
                    jsonResponse.get("resultCode").toString()
                } else {
                    "EMPTY"
                }
                DropInServiceResult.Finished(resultCode)
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
