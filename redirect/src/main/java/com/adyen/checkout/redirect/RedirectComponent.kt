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
import com.adyen.checkout.components.ActionComponentProvider
import com.adyen.checkout.components.base.BaseActionComponent
import com.adyen.checkout.components.base.IntentHandlingComponent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException

class RedirectComponent(
    savedStateHandle: SavedStateHandle,
    application: Application,
    configuration: RedirectConfiguration,
    private val redirectDelegate: RedirectDelegate
) : BaseActionComponent<RedirectConfiguration>(savedStateHandle, application, configuration), IntentHandlingComponent {

    override fun canHandleAction(action: Action): Boolean {
        return PROVIDER.canHandleAction(action)
    }

    @Throws(ComponentException::class)
    override fun handleActionInternal(activity: Activity, action: Action) {
        val redirectAction = action as RedirectAction
        redirectDelegate.makeRedirect(activity, redirectAction)
    }

    /**
     * Call this method when receiving the return URL from the redirect with the result data.
     * This result will be in the [Intent.getData] and begins with the returnUrl you specified on the payments/ call.
     *
     * @param intent The received [Intent].
     */
    override fun handleIntent(intent: Intent) {
        try {
            val parsedResult = redirectDelegate.handleRedirectResponse(intent.data)
            notifyDetails(parsedResult)
        } catch (e: CheckoutException) {
            notifyException(e)
        }
    }

    companion object {
        @JvmField
        val PROVIDER: ActionComponentProvider<RedirectComponent, RedirectConfiguration> = RedirectComponentProvider()

        /**
         * Returns the suggested value to be used as the `returnUrl` value in the payments/ call.
         *
         * @param context The context provides the package name which constitutes part of the ReturnUrl
         * @return The suggested `returnUrl` to be used. Consists of [RedirectUtil.REDIRECT_RESULT_SCHEME] + App package name.
         */
        fun getReturnUrl(context: Context): String {
            return RedirectUtil.REDIRECT_RESULT_SCHEME + context.packageName
        }
    }
}
