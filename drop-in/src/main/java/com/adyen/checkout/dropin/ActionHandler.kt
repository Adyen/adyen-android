/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.dropin

import android.arch.lifecycle.Observer
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.wechatpay.WeChatPayActionComponent

class ActionHandler(activity: FragmentActivity, private val callback: DetailsRequestedInterface) : Observer<ActionComponentData> {

    companion object {
        val TAG = LogUtil.getTag()
        const val UNKNOWN_ACTION = "UNKNOWN ACTION"
    }

    private val redirectComponent = RedirectComponent.PROVIDER.get(activity)
    private val adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(activity)
    private val weChatPayActionComponent = WeChatPayActionComponent.PROVIDER.get(activity)

    init {
        redirectComponent.observe(activity, this)
        adyen3DS2Component.observe(activity, this)
        weChatPayActionComponent.observe(activity, this)

        redirectComponent.observeErrors(activity, Observer {
            callback.onError(it?.errorMessage ?: "Redirect Error.")
        })

        adyen3DS2Component.observeErrors(activity, Observer {
            callback.onError(it?.errorMessage ?: "3DS2 Error.")
        })
        weChatPayActionComponent.observeErrors(activity, Observer {
            callback.onError(it?.errorMessage ?: "WechatPay Error.")
        })
    }

    override fun onChanged(componentData: ActionComponentData?) {
        if (componentData != null) {
            callback.requestDetailsCall(componentData)
        }
    }

    fun saveState(bundle: Bundle?) {
        redirectComponent.saveState(bundle)
        adyen3DS2Component.saveState(bundle)
    }

    fun restoreState(bundle: Bundle?) {
        redirectComponent.restoreState(bundle)
        adyen3DS2Component.restoreState(bundle)
    }

    fun handleAction(activity: FragmentActivity, action: Action, sendResult: (String) -> Unit) {
        when {
            redirectComponent.canHandleAction(action) -> {
                redirectComponent.handleAction(activity, action)
            }
            adyen3DS2Component.canHandleAction(action) -> {
                adyen3DS2Component.handleAction(activity, action)
            }
            weChatPayActionComponent.canHandleAction(action) -> {
                weChatPayActionComponent.handleAction(activity, action)
            }
            else -> {
                Logger.e(TAG, "Unknown Action - ${action.type}")
                sendResult("$UNKNOWN_ACTION.${action.type}")
            }
        }
    }

    fun handleRedirectResponse(data: Uri) {
        redirectComponent.handleRedirectResponse(data)
    }

    fun handleWeChatPayResponse(intent: Intent) {
        weChatPayActionComponent.handleResultIntent(intent)
    }

    interface DetailsRequestedInterface {
        fun requestDetailsCall(actionComponentData: ActionComponentData)
        fun onError(errorMessage: String)
    }
}
