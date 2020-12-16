/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.base.util.ActionTypes
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.wechatpay.WeChatPayActionComponent

class ActionHandler(
    activity: FragmentActivity,
    private val callback: ActionHandlingInterface,
    private val dropInConfiguration: DropInConfiguration
) : Observer<ActionComponentData> {

    companion object {
        val TAG = LogUtil.getTag()
        const val UNKNOWN_ACTION = "UNKNOWN ACTION"
    }

    // Actions which will be handled by the Fragment with it's associated view
    private val viewableActionTypes = listOf(ActionTypes.AWAIT)

    private val redirectComponent = RedirectComponent.PROVIDER.get(
        activity,
        activity.application,
        dropInConfiguration.getConfigurationFor(ActionTypes.REDIRECT, activity)
    )
    private val adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(
        activity,
        activity.application,
        dropInConfiguration.getConfigurationFor(ActionTypes.THREEDS2, activity)
    )
    // get config from Drop-in when available
    private val weChatPayActionComponent = WeChatPayActionComponent.PROVIDER.get(activity, activity.application, null)

    init {
        redirectComponent.observe(activity, this)
        adyen3DS2Component.observe(activity, this)
        weChatPayActionComponent.observe(activity, this)

        redirectComponent.observeErrors(
            activity,
            {
                callback.onActionError(it?.errorMessage ?: "Redirect Error.")
            }
        )

        adyen3DS2Component.observeErrors(
            activity,
            {
                callback.onActionError(it?.errorMessage ?: "3DS2 Error.")
            }
        )
        weChatPayActionComponent.observeErrors(
            activity,
            {
                callback.onActionError(it?.errorMessage ?: "WechatPay Error.")
            }
        )
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
            viewableActionTypes.contains(action.type) -> {
                callback.displayAction(action)
            }
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

    interface ActionHandlingInterface {
        fun displayAction(action: Action)
        // Same signature as the Fragment Protocol interface
        fun requestDetailsCall(actionComponentData: ActionComponentData)
        fun onActionError(errorMessage: String)
    }
}
