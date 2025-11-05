/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/1/2021.
 */

package com.adyen.checkout.example.service

import android.util.Log
import com.adyen.checkout.card.old.CardComponentState
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.StoredPaymentMethod
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.dropin.old.DropInService
import com.adyen.checkout.dropin.old.DropInServiceResult
import com.adyen.checkout.dropin.old.ErrorDialog
import com.adyen.checkout.dropin.old.RecurringDropInServiceResult
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.extensions.IODispatcher
import com.adyen.checkout.example.extensions.getLogTag
import com.adyen.checkout.example.extensions.toStringPretty
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 */
@AndroidEntryPoint
class ExampleDropInService : DropInService() {

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun onSubmit(
        state: PaymentComponentState<*>
    ) {
        launch(IODispatcher) {
            Log.d(TAG, "onPaymentsCallRequested")

            checkPaymentState(state)

            val paymentComponentJson = PaymentComponentData.SERIALIZER.serialize(state.data)
            // Check out the documentation of this method on the parent DropInService class
            val paymentRequest = createPaymentRequest(
                paymentComponentData = paymentComponentJson,
                shopperReference = keyValueStorage.getShopperReference(),
                amount = state.data.amount,
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
    }

    /**
     * This is an example on how to handle the PaymentComponentState
     */
    private fun checkPaymentState(paymentComponentState: PaymentComponentState<*>) {
        @Suppress("ControlFlowWithEmptyBody")
        if (paymentComponentState is CardComponentState) {
            // a card payment is being made, handle accordingly
        }
    }

    override fun onAdditionalDetails(actionComponentData: ActionComponentData) {
        launch(IODispatcher) {
            Log.d(TAG, "onDetailsCallRequested")

            val actionComponentJson = ActionComponentData.SERIALIZER.serialize(actionComponentData)

            Log.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

            val response = paymentsRepository.makeDetailsRequest(actionComponentJson)

            val result = handleResponse(response)
            sendResult(result)
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

    override fun onRemoveStoredPaymentMethod(
        storedPaymentMethod: StoredPaymentMethod,
    ) {
        launch(IODispatcher) {
            val storedPaymentMethodId = storedPaymentMethod.id.orEmpty()
            val isSuccessfullyRemoved = paymentsRepository.removeStoredPaymentMethod(
                storedPaymentMethodId = storedPaymentMethodId,
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
            )
            val result = handleRemoveStoredPaymentMethodResult(storedPaymentMethodId, isSuccessfullyRemoved)
            sendRecurringResult(result)
        }
    }

    private fun handleRemoveStoredPaymentMethodResult(
        storedPaymentMethodId: String,
        isSuccessfullyRemoved: Boolean,
    ): RecurringDropInServiceResult {
        return if (isSuccessfullyRemoved) {
            Log.v(TAG, "removeStoredPaymentMethod response successful")
            RecurringDropInServiceResult.PaymentMethodRemoved(storedPaymentMethodId)
        } else {
            Log.e(TAG, "FAILED")
            RecurringDropInServiceResult.Error(errorDialog = ErrorDialog(message = "IOException"))
        }
    }

    companion object {
        private val TAG = getLogTag()
    }
}
