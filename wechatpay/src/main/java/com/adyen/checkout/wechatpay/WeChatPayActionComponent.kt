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
import com.adyen.checkout.core.exception.ComponentException

class WeChatPayActionComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: WeChatPayActionConfiguration,
    private val weChatDelegate: WeChatDelegate,
) : BaseActionComponent<WeChatPayActionConfiguration>(savedStateHandle, application, configuration),
    IntentHandlingComponent {

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

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        weChatDelegate.handleAction(action, activity.javaClass.name)
    }

    companion object {
        val PROVIDER: ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration> =
            WeChatPayActionComponentProvider()
    }
}
