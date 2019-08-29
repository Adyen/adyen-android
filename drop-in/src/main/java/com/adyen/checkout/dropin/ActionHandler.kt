/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.dropin

import android.arch.lifecycle.Observer
import android.net.Uri
import android.support.v4.app.FragmentActivity
import com.adyen.checkout.adyen3ds2.Adyen3DS2Component
import com.adyen.checkout.base.ActionComponentData
import com.adyen.checkout.base.model.payments.response.Action
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.RedirectComponent

class ActionHandler(activity: FragmentActivity, private val callback: DetailsRequestedInterface) : Observer<ActionComponentData> {

    companion object {
        val TAG = LogUtil.getTag()
        const val UNKNOWN_ACTION = "UNKNOWN ACTION"
    }

    private val redirectComponent: RedirectComponent = RedirectComponent.PROVIDER.get(activity)
    private val adyen3DS2Component: Adyen3DS2Component = Adyen3DS2Component.PROVIDER.get(activity)

    init {
        redirectComponent.observe(activity, this)
        adyen3DS2Component.observe(activity, this)

        redirectComponent.observeErrors(activity, Observer {
            callback.onError(it?.errorMessage ?: "Redirect Error.")
        })

        adyen3DS2Component.observeErrors(activity, Observer {
            callback.onError(it?.errorMessage ?: "3DS2 Error.")
        })
    }

    override fun onChanged(componentData: ActionComponentData?) {
        if (componentData != null) {
            callback.requestDetailsCall(componentData)
        }
    }

    fun handleAction(activity: FragmentActivity, action: Action, sendResult: (String) -> Unit) {
        when {
            redirectComponent.canHandleAction(action) -> {
                redirectComponent.handleAction(activity, action)
            }
            adyen3DS2Component.canHandleAction(action) -> {
                adyen3DS2Component.handleAction(activity, action)
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

    interface DetailsRequestedInterface {
        fun requestDetailsCall(actionComponentData: ActionComponentData)
        fun onError(errorMessage: String)
    }
}
