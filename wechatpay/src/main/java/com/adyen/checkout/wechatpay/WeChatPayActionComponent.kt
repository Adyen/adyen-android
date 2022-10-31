/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/10/2019.
 */
package com.adyen.checkout.wechatpay

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponent
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.ui.ViewableComponent
import com.adyen.checkout.components.ui.view.ComponentViewType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class WeChatPayActionComponent(
    override val configuration: WeChatPayActionConfiguration,
    override val delegate: WeChatDelegate,
) : ViewModel(),
    ActionComponent<WeChatPayActionConfiguration>,
    IntentHandlingComponent,
    ViewableComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    override fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        delegate.detailsFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(ActionComponentEvent.ActionDetails(it)) }
            .launchIn(viewModelScope)

        delegate.exceptionFlow
            .flowWithLifecycle(lifecycleOwner.lifecycle)
            .onEach { callback(ActionComponentEvent.Error(ComponentError(it))) }
            .launchIn(viewModelScope)
    }

    /**
     * Pass the result Intent from the WeChatPay SDK response on Activity#onNewIntent(Intent).
     * You can check if the Intent is correct by calling [WeChatPayUtils.isResultIntent]
     *
     * @param intent The intent result from WeChatPay SDK.
     */
    override fun handleIntent(intent: Intent) {
        delegate.handleIntent(intent)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<WeChatPayActionComponent, WeChatPayActionConfiguration, WeChatDelegate> =
            WeChatPayActionComponentProvider()
    }
}
