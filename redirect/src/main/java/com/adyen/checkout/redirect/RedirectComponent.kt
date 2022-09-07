/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/4/2019.
 */
package com.adyen.checkout.redirect

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.core.exception.ComponentException
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RedirectComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: RedirectConfiguration,
    private val redirectDelegate: RedirectDelegate
) : BaseActionComponent<RedirectConfiguration>(savedStateHandle, application, configuration), IntentHandlingComponent {

    init {
        redirectDelegate.detailsFlow
            .filterNotNull()
            .onEach { notifyDetails(it) }
            .launchIn(viewModelScope)

        redirectDelegate.exceptionFlow
            .onEach { notifyException(it) }
            .launchIn(viewModelScope)
    }

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    override fun handleActionInternal(action: Action, activity: Activity) {
        if (action !is RedirectAction) {
            notifyException(ComponentException("Unsupported action"))
            return
        }
        redirectDelegate.handleAction(action, activity)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        redirectDelegate.handleIntent(intent)
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
