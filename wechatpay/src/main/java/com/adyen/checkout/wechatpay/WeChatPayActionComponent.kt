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
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.SdkAction
import com.adyen.checkout.components.model.payments.response.WeChatPaySdkData
import com.adyen.checkout.core.exception.ComponentException
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WeChatPayActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: WeChatPayActionConfiguration,
    private val weChatDelegate: WeChatDelegate,
) : BaseActionComponent<WeChatPayActionConfiguration>(savedStateHandle, application, configuration),
    IntentHandlingComponent {

    init {
        weChatDelegate.detailsFlow
            .onEach { notifyDetails(it) }
            .launchIn(viewModelScope)

        weChatDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    /**
     * Pass the result Intent from the WeChatPay SDK response on Activity#onNewIntent(Intent).
     * You can check if the Intent is correct by calling [WeChatPayUtils.isResultIntent]
     *
     * @param intent The intent result from WeChatPay SDK.
     */
    override fun handleIntent(intent: Intent) {
        weChatDelegate.handleIntent(intent)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        @Suppress("UNCHECKED_CAST")
        val sdkAction = (action as? SdkAction<WeChatPaySdkData>)
        if (sdkAction == null) {
            notifyException(ComponentException("Unsupported action"))
            return
        }
        weChatDelegate.handleAction(action, activity)
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration, WeChatDelegate> =
            WeChatPayActionComponentProvider()
    }
}
