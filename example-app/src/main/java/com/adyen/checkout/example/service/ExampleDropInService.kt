/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.DropInService
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import org.json.JSONObject
import javax.inject.Inject

/**
 * This is just an example on how to make networkModule calls on the [DropInService].
 * You should make the calls to your own servers and have additional data or processing if necessary.
 *
 * This class implements [makePaymentsCall] and [makeDetailsCall] which provide the simplest way of
 * interacting with Drop-in, without having to manage background threads.
 */
@AndroidEntryPoint
class ExampleDropInService : DropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun makePaymentsCall(paymentComponentJson: JSONObject): DropInServiceResult {
        Logger.d(TAG, "makePaymentsCall")

        // Check out the documentation of this method on the parent DropInService class
        val paymentRequest = createPaymentRequest(
            paymentComponentData = paymentComponentJson,
            shopperReference = keyValueStorage.getShopperReference(),
            amount = keyValueStorage.getAmount(),
            countryCode = keyValueStorage.getCountry(),
            merchantAccount = keyValueStorage.getMerchantAccount(),
            redirectUrl = RedirectComponent.getReturnUrl(applicationContext),
            isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
            isExecuteThreeD = keyValueStorage.isExecuteThreeD(),
            shopperEmail = keyValueStorage.getShopperEmail()
        )

        Logger.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
        val call = paymentsRepository.paymentsRequest(paymentRequest)

        return handleResponse(call)
    }

    override fun makeDetailsCall(actionComponentJson: JSONObject): DropInServiceResult {
        Logger.d(TAG, "makeDetailsCall")

        Logger.v(TAG, "payments/details/ - ${actionComponentJson.toStringPretty()}")

        return handleResponse(paymentsRepository.detailsRequest(actionComponentJson))
    }

    @Suppress("NestedBlockDepth")
    private fun handleResponse(detailsResponse: JSONObject?): DropInServiceResult {
        return if (detailsResponse != null) {
            if (detailsResponse.has("action")) {
                val action = Action.SERIALIZER.deserialize(detailsResponse.getJSONObject("action"))
                DropInServiceResult.Action(action)
            } else {
                Logger.d(TAG, "Final result - ${detailsResponse.toStringPretty()}")

                val resultCode = if (detailsResponse.has("resultCode")) {
                    detailsResponse.get("resultCode").toString()
                } else {
                    "EMPTY"
                }
                DropInServiceResult.Finished(resultCode)
            }
        } else {
            DropInServiceResult.Error(reason = "IOException")
        }
    }
}
