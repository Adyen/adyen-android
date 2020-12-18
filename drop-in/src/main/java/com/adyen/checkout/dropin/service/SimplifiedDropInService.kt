/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/10/2019.
 */

package com.adyen.checkout.dropin.service

import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.model.JsonUtils
import org.json.JSONObject

abstract class SimplifiedDropInService : DropInService() {

    companion object {
        protected val TAG = LogUtil.getTag()
        private const val ACTION_KEY = "action"
        private const val RESULT_CODE_KEY = "resultCode"
    }

    override fun makePaymentsCall(paymentComponentData: JSONObject): CallResult {
        val result = makePaymentsCallOrFail(paymentComponentData)
        return handleResponse(result)
    }

    override fun makeDetailsCall(actionComponentData: JSONObject): CallResult {
        val result = makeDetailsCallOrFail(actionComponentData)
        return handleResponse(result)
    }

    abstract fun makePaymentsCallOrFail(paymentComponentData: JSONObject): JSONObject?

    abstract fun makeDetailsCallOrFail(actionComponentData: JSONObject): JSONObject?

    private fun handleResponse(response: JSONObject?): CallResult {
        Logger.v(TAG, "handleResponse - ${JsonUtils.indent(response)}")

        if (response != null && response.keys().hasNext()) {
            return when {
                response.has(ACTION_KEY) -> {
                    Logger.d(TAG, "has action")
                    CallResult(CallResult.ResultType.ACTION, response.get(ACTION_KEY).toString())
                }
                isAction(response) -> {
                    Logger.d(TAG, "is action")
                    CallResult(CallResult.ResultType.ACTION, response.toString())
                }
                response.has(RESULT_CODE_KEY) -> {
                    val resultCode = response.getString(RESULT_CODE_KEY)
                    Logger.d(TAG, "Final resultCode - $resultCode")
                    CallResult(CallResult.ResultType.FINISHED, resultCode)
                }
                else -> {
                    Logger.e(TAG, "Unexpected response - ${JsonUtils.indent(response)}")
                    CallResult(CallResult.ResultType.ERROR, "Unexpected response")
                }
            }
        } else {
            Logger.e(TAG, "Response is empty")
            return CallResult(CallResult.ResultType.ERROR, "Response Error")
        }
    }

    private fun isAction(jsonObject: JSONObject): Boolean {
        return jsonObject.has(Action.TYPE) && jsonObject.has(Action.PAYMENT_METHOD_TYPE)
    }
}
