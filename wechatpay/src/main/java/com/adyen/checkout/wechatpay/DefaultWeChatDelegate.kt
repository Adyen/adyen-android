/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay

import android.content.Intent
import androidx.annotation.VisibleForTesting
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.json.JSONException
import org.json.JSONObject

internal class DefaultWeChatDelegate(
    private val iwxApi: IWXAPI,
    private val payRequestGenerator: WeChatRequestGenerator<*>
) : WeChatDelegate {

    private val _detailsFlow = MutableSharedFlow<JSONObject>(0, 1, BufferOverflow.DROP_OLDEST)
    override val detailsFlow: Flow<JSONObject> = _detailsFlow

    private val _exceptionFlow = MutableSharedFlow<CheckoutException>(0, 1, BufferOverflow.DROP_OLDEST)
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    private val eventHandler = object : IWXAPIEventHandler {
        override fun onReq(baseReq: BaseReq) = Unit

        override fun onResp(baseResp: BaseResp) {
            onResponse(baseResp)
        }
    }

    @VisibleForTesting
    internal fun onResponse(baseResponse: BaseResp) {
        parseResult(baseResponse)?.let { response ->
            _detailsFlow.tryEmit(response)
        }
    }

    private fun parseResult(baseResp: BaseResp): JSONObject? {
        val result = JSONObject()
        try {
            result.put(RESULT_CODE, baseResp.errCode)
        } catch (e: JSONException) {
            _exceptionFlow.tryEmit(CheckoutException("Error parsing result.", e))
            return null
        }
        return result
    }

    override fun handleIntent(intent: Intent) {
        iwxApi.handleIntent(intent, eventHandler)
    }

    override fun handleAction(action: Action, activityName: String) {
        Logger.d(TAG, "handleAction: activity - $activityName")

        @Suppress("UNCHECKED_CAST")
        val sdkData = (action as? SdkAction<WeChatPaySdkData>)?.sdkData ?: run {
            _exceptionFlow.tryEmit(ComponentException("sdkData is null"))
            return@handleAction
        }

        val isWeChatNotInitiated = !initiateWeChatPayRedirect(sdkData, activityName)

        if (isWeChatNotInitiated) {
            _exceptionFlow.tryEmit(ComponentException("Failed to initialize WeChat app"))
            return
        }
    }

    private fun initiateWeChatPayRedirect(weChatPaySdkData: WeChatPaySdkData, activityName: String): Boolean {
        Logger.d(TAG, "initiateWeChatPayRedirect")
        iwxApi.registerApp(weChatPaySdkData.appid)
        val request = payRequestGenerator.generate(weChatPaySdkData, activityName)
        return iwxApi.sendReq(request)
    }

    companion object {
        private val TAG = LogUtil.getTag()

        private const val RESULT_CODE = "resultCode"
    }
}
