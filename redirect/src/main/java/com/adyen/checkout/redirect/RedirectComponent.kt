/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/4/2019.
 */
package com.adyen.checkout.redirect

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.core.RedirectableActionComponent
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.components.core.internal.ActionComponent
import com.adyen.checkout.components.core.internal.ActionComponentEvent
import com.adyen.checkout.components.core.internal.ActionComponentEventHandler
import com.adyen.checkout.components.core.internal.IntentHandlingComponent
import com.adyen.checkout.components.core.internal.provider.ActionComponentProvider
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.redirect.internal.provider.RedirectComponentProvider
import com.adyen.checkout.redirect.internal.ui.RedirectDelegate
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import com.adyen.checkout.ui.core.internal.ui.ViewableComponent
import kotlinx.coroutines.flow.Flow

/**
 * An [ActionComponent] that is able to handle the 'redirect' action.
 */
class RedirectComponent internal constructor(
    override val delegate: RedirectDelegate,
    internal val actionComponentEventHandler: ActionComponentEventHandler,
) : ViewModel(),
    ActionComponent,
    IntentHandlingComponent,
    ViewableComponent,
    RedirectableActionComponent {

    override val viewFlow: Flow<ComponentViewType?> = delegate.viewFlow

    init {
        delegate.initialize(viewModelScope)
    }

    internal fun observe(lifecycleOwner: LifecycleOwner, callback: (ActionComponentEvent) -> Unit) {
        delegate.observe(lifecycleOwner, viewModelScope, callback)
    }

    internal fun removeObserver() {
        delegate.removeObserver()
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleAction(action: Action, activity: Activity) {
        delegate.handleAction(action, activity)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        delegate.handleIntent(intent)
    }

    override fun setOnRedirectListener(listener: () -> Unit) {
        delegate.setOnRedirectListener(listener)
    }

    override fun onCleared() {
        super.onCleared()
        adyenLog(AdyenLogLevel.DEBUG) { "onCleared" }
        delegate.onCleared()
    }

    companion object {

        @JvmField
        val PROVIDER: ActionComponentProvider<RedirectComponent, RedirectConfiguration, RedirectDelegate> =
            RedirectComponentProvider()

        /**
         * The suggested scheme to be used in the intent filter to receive the redirect result.
         * This value should be the beginning of the `returnUrl` sent on the payments/ call.
         */
        const val REDIRECT_RESULT_SCHEME = BuildConfig.checkoutRedirectScheme + "://"

        /**
         * Returns the suggested value to be used as the `returnUrl` value in the payments/ call.
         *
         * @param context The context provides the package name which constitutes part of the ReturnUrl
         * @return The suggested `returnUrl` to be used. Consists of [REDIRECT_RESULT_SCHEME] + App package name.
         */
        fun getReturnUrl(context: Context): String {
            return REDIRECT_RESULT_SCHEME + context.packageName
        }
    }
}
