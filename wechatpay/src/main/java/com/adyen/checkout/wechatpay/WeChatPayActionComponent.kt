/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/10/2019.
 */
package com.adyen.checkout.wechatpay

import android.app.Activity
import android.app.Application
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil.getTag
import com.adyen.checkout.core.log.Logger
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WeChatPayActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: WeChatPayActionConfiguration
) : BaseActionComponent<WeChatPayActionConfiguration>(savedStateHandle, application, configuration),
    IntentHandlingComponent {

    private val iwxApi: IWXAPI = WXAPIFactory.createWXAPI(application, null, true)
    private val eventHandler: IWXAPIEventHandler = object : IWXAPIEventHandler {
        override fun onReq(baseReq: BaseReq) {
            // Do nothing.
        }

        override fun onResp(baseResp: BaseResp) {
            notifyDetails(WeChatPayUtils.parseResult(baseResp))
        }
    }

    /**
     * Pass the result Intent from the WeChatPay SDK response on Activity#onNewIntent(Intent).
     * You can check if the Intent is correct by calling [WeChatPayUtils.isResultIntent]
     *
     * @param intent The intent result from WeChatPay SDK.
     */
    override fun handleIntent(intent: Intent) {
        // TODO check intent identifiers
        iwxApi.handleIntent(intent, eventHandler)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        Logger.d(TAG, "handleActionInternal: activity - " + activity.localClassName)
        @Suppress("UNCHECKED_CAST")
        val sdkData = (action as SdkAction<WeChatPaySdkData>).sdkData ?: throw ComponentException("sdkData is null")
        val weChatInitiated = initiateWeChatPayRedirect(sdkData, activity.javaClass.name)
        if (!weChatInitiated) {
            throw ComponentException("Failed to initialize WeChat app.")
        }
    }

    private fun initiateWeChatPayRedirect(weChatPaySdkData: WeChatPaySdkData, callbackActivityName: String): Boolean {
        Logger.d(TAG, "initiateWeChatPayRedirect")
        iwxApi.registerApp(weChatPaySdkData.appid)
        return iwxApi.sendReq(WeChatPayUtils.generatePayRequest(weChatPaySdkData, callbackActivityName))
    }

    companion object {
        private val TAG = getTag()
        val PROVIDER: ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration> =
            WeChatPayActionComponentProvider()
    }
}
