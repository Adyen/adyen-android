/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/8/2022.
 */

package com.adyen.checkout.redirect

import android.app.Activity
import android.content.Intent
import android.net.Uri
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.exception.ComponentException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class DefaultRedirectDelegate : RedirectDelegate {

    private val _detailsFlow = MutableSharedFlow<JSONObject>(0, 1, BufferOverflow.DROP_OLDEST)
    override val detailsFlow: Flow<JSONObject> = _detailsFlow

    private val _exceptionFlow = MutableSharedFlow<CheckoutException>(0, 1, BufferOverflow.DROP_OLDEST)
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    override fun handleAction(activity: Activity, redirectAction: RedirectAction) {
        makeRedirect(activity, redirectAction.url)
    }

    private fun makeRedirect(activity: Activity, url: String?) {
        try {
            Logger.d(TAG, "makeRedirect - $url")
            if (!url.isNullOrEmpty()) {
                val redirectUri = Uri.parse(url)
                // TODO look into emitting a value to tell observers that a redirect was launched so they can track its
                //  status when the app resumes. Currently we have no way of doing that but we can create something like
                //  PaymentComponentState for actions.
                RedirectUtil.launchUriRedirect(activity, redirectUri)
            } else {
                throw ComponentException("Redirect URL is empty.")
            }
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }

    override fun handleIntent(intent: Intent) {
        try {
            val data = intent.data ?: throw ComponentException("Received a null redirect Uri")
            val details = RedirectUtil.parseRedirectResult(data)
            _detailsFlow.tryEmit(details)
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }
}
