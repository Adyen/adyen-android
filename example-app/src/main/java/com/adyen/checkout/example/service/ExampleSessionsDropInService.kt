/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 31/5/2022.
 */

package com.adyen.checkout.example.service

import com.adyen.checkout.card.CardComponentState
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.BlikPaymentMethod
import com.adyen.checkout.components.model.payments.request.OrderRequest
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.OrderResponse
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.toStringPretty
import com.adyen.checkout.dropin.service.DropInServiceResult
import com.adyen.checkout.dropin.service.SessionDropInService
import com.adyen.checkout.example.data.storage.KeyValueStorage
import com.adyen.checkout.example.repositories.PaymentsRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@AndroidEntryPoint
class ExampleSessionsDropInService: SessionDropInService() {

    companion object {
        private val TAG = LogUtil.getTag()
    }

    @Inject
    lateinit var paymentsRepository: PaymentsRepository

    @Inject
    lateinit var keyValueStorage: KeyValueStorage

    override fun makePaymentsCallMerchant(paymentComponentState: PaymentComponentState<*>, paymentComponentJson: JSONObject): Boolean {
        return if (paymentComponentState.data.paymentMethod is BlikPaymentMethod || paymentComponentState is CardComponentState) {
            launch(Dispatchers.IO) {
                Logger.d(TAG, "onPaymentsCallRequested")

                // Check out the documentation of this method on the parent DropInService class
                val paymentRequest = createPaymentRequest(
                    paymentComponentData = paymentComponentJson,
                    shopperReference = keyValueStorage.getShopperReference(),
                    amount = keyValueStorage.getAmount(),
                    countryCode = keyValueStorage.getCountry(),
                    merchantAccount = keyValueStorage.getMerchantAccount(),
                    redirectUrl = RedirectComponent.getReturnUrl(applicationContext),
                    isThreeds2Enabled = keyValueStorage.isThreeds2Enable(),
                    isExecuteThreeD = keyValueStorage.isExecuteThreeD()
                )

                Logger.v(TAG, "paymentComponentJson - ${paymentComponentJson.toStringPretty()}")
                val response = paymentsRepository.paymentsRequestAsync(paymentRequest)

                val result = handleResponse(response) ?: return@launch
                sendResult(result)
            }
            true
        } else {
            false
        }
    }

    override fun makeDetailsCallMerchant(actionComponentData: ActionComponentData, actionComponentJson: JSONObject): Boolean {
        return if (isFlowTakenOver) {
            launch(Dispatchers.IO) {
                Logger.d(TAG, "onDetailsCallRequested")

                val response = paymentsRepository.detailsRequestAsync(ActionComponentData.SERIALIZER.serialize(actionComponentData))

                val result = handleResponse(response) ?: return@launch
                sendResult(result)
            }
            true
        } else {
            false
        }
    }

    override fun makeCreateOrderMerchant(): Boolean {
        return super.makeCreateOrderMerchant()
    }

    override fun makeCheckBalanceCallMerchant(paymentMethodData: PaymentMethodDetails): Boolean {
        return super.makeCheckBalanceCallMerchant(paymentMethodData)
    }

    override fun makeCancelOrderCallMerchant(order: OrderRequest, shouldUpdatePaymentMethods: Boolean): Boolean {
        return super.makeCancelOrderCallMerchant(order, shouldUpdatePaymentMethods)
    }

    private fun handleResponse(jsonResponse: JSONObject?): DropInServiceResult? {
        return when {
            jsonResponse == null -> {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            isAction(jsonResponse) -> {
                Logger.d(TAG, "Received action")
                val action = Action.SERIALIZER.deserialize(jsonResponse.getJSONObject("action"))
                DropInServiceResult.Action(action)
            }
            isNonFullyPaidOrder(jsonResponse) -> {
                Logger.d(TAG, "Received a non fully paid order")
                val order = getOrderFromResponse(jsonResponse)
                fetchPaymentMethods(order)
                null
            }
            else -> {
                Logger.d(TAG, "Final result - ${jsonResponse.toStringPretty()}")
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

    private fun isNonFullyPaidOrder(jsonResponse: JSONObject): Boolean {
        return jsonResponse.has("order") && getOrderFromResponse(jsonResponse).remainingAmount?.value ?: 0 > 0
    }

    private fun getOrderFromResponse(jsonResponse: JSONObject): OrderResponse {
        val orderJSON = jsonResponse.getJSONObject("order")
        return OrderResponse.SERIALIZER.deserialize(orderJSON)
    }

    private fun fetchPaymentMethods(order: OrderResponse? = null) {
        Logger.d(TAG, "fetchPaymentMethods")
        launch(Dispatchers.IO) {
            val orderRequest = if (order == null) null else OrderRequest(
                pspReference = order.pspReference,
                orderData = order.orderData
            )
            val paymentMethodRequest = getPaymentMethodRequest(
                merchantAccount = keyValueStorage.getMerchantAccount(),
                shopperReference = keyValueStorage.getShopperReference(),
                amount = keyValueStorage.getAmount(),
                countryCode = keyValueStorage.getCountry(),
                shopperLocale = keyValueStorage.getShopperLocale(),
                splitCardFundingSources = keyValueStorage.isSplitCardFundingSources(),
                order = orderRequest
            )
            val paymentMethods = paymentsRepository.getPaymentMethods(paymentMethodRequest)
            val result = if (paymentMethods != null) {
                DropInServiceResult.Update(paymentMethods, order)
            } else {
                Logger.e(TAG, "FAILED")
                DropInServiceResult.Error(reason = "IOException")
            }
            sendResult(result)
        }
    }

}
