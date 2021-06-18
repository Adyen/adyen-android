/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/5/2019.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger

class ActionHandler(
    private val callback: ActionHandlingInterface,
    private val dropInConfiguration: DropInConfiguration
) : Observer<ActionComponentData> {

    companion object {
        val TAG = LogUtil.getTag()
        const val UNKNOWN_ACTION = "UNKNOWN ACTION"
    }

    private var loadedComponent: BaseActionComponent<*>? = null

    override fun onChanged(componentData: ActionComponentData?) {
        if (componentData != null) {
            callback.requestDetailsCall(componentData)
        }
    }

    fun saveState(bundle: Bundle?) {
        loadedComponent?.saveState(bundle)
    }

    fun restoreState(bundle: Bundle?) {
        loadedComponent?.restoreState(bundle)
    }

    fun handleAction(activity: FragmentActivity, action: Action, sendResult: (String) -> Unit) {
        Logger.d(TAG, "handleAction - ${action.type}")
        val provider = getActionProviderFor(action)
        if (provider == null) {
            Logger.e(TAG, "Unknown Action - ${action.type}")
            sendResult("$UNKNOWN_ACTION.${action.type}")
            return
        }

        if (provider.requiresView(action)) {
            Logger.d(TAG, "handleAction - action is viewable, requesting displayAction callback")
            callback.displayAction(action)
        } else {
            getActionComponentFor(activity, provider, dropInConfiguration).apply {
                loadedComponent = this
                handleAction(activity, action)
                observe(activity, this@ActionHandler)
                observeErrors(activity, { callback.onActionError(it?.errorMessage ?: "Error handling action") })
                Logger.d(TAG, "handleAction - loaded a new component - ${this::class.java.simpleName}")
            }
        }
    }

    fun handleRedirectResponse(intent: Intent) {
        handleIntent(intent)
    }

    fun handleWeChatPayResponse(intent: Intent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        val component = loadedComponent ?: throw CheckoutException("Action component is not loaded")
        Logger.d(TAG, "handleAction - loaded a new component - ${component::class.java.simpleName}")
        if (component !is IntentHandlingComponent) throw CheckoutException("Loaded component cannot handle intents")
        component.handleIntent(intent)
    }

    interface ActionHandlingInterface {
        fun displayAction(action: Action)

        // Same signature as the Fragment Protocol interface
        fun requestDetailsCall(actionComponentData: ActionComponentData)
        fun onActionError(errorMessage: String)
    }
}
