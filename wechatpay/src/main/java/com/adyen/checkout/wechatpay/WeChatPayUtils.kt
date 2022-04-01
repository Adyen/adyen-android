/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/9/2019.
 */
package com.adyen.checkout.wechatpay

import android.app.Application
import android.content.Intent
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.adyen.checkout.core.exception.CheckoutException
import com.tencent.mm.opensdk.constants.Build
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import org.json.JSONException
import org.json.JSONObject

object WeChatPayUtils {

    private const val RESULT_EXTRA_KEY = "_wxapi_baseresp_errstr"
    private const val RESULT_CODE = "resultCode"

    fun isResultIntent(intent: Intent?): Boolean {
        return intent?.extras?.containsKey(RESULT_EXTRA_KEY) == true
    }

    fun isAvailable(applicationContext: Application?): Boolean {
        val api = WXAPIFactory.createWXAPI(applicationContext, null, true)
        val isAppInstalled = api.isWXAppInstalled
        val isSupported = Build.PAY_SUPPORTED_SDK_INT <= api.wxAppSupportAPI
        api.detach()
        return isAppInstalled && isSupported
    }

    fun generatePayRequest(weChatPaySdkData: WeChatPaySdkData, callbackActivityName: String): PayReq {
        return PayReq().apply {
            appId = weChatPaySdkData.appid
            partnerId = weChatPaySdkData.partnerid
            prepayId = weChatPaySdkData.prepayid
            packageValue = weChatPaySdkData.packageValue
            nonceStr = weChatPaySdkData.noncestr
            timeStamp = weChatPaySdkData.timestamp
            sign = weChatPaySdkData.sign
            options = PayReq.Options()
            options.callbackClassName = callbackActivityName
        }
    }

    fun parseResult(baseResp: BaseResp): JSONObject {
        val result = JSONObject()
        try {
            result.put(RESULT_CODE, baseResp.errCode)
        } catch (e: JSONException) {
            throw CheckoutException("Error parsing result.", e)
        }
        return result
    }
}
