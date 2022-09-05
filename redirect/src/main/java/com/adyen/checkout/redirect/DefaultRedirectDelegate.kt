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
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.flow.MutableSingleEventSharedFlow
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.components.repository.PaymentDataRepository
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.redirect.handler.RedirectHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import org.json.JSONObject

private val TAG = LogUtil.getTag()

internal class DefaultRedirectDelegate(
    private val redirectHandler: RedirectHandler,
    private val paymentDataRepository: PaymentDataRepository,
) : RedirectDelegate {

    private val _detailsFlow: MutableSharedFlow<ActionComponentData> = MutableSingleEventSharedFlow()
    override val detailsFlow: Flow<ActionComponentData> = _detailsFlow

    private val _exceptionFlow: MutableSharedFlow<CheckoutException> = MutableSingleEventSharedFlow()
    override val exceptionFlow: Flow<CheckoutException> = _exceptionFlow

    override fun handleAction(action: RedirectAction, activity: Activity) {
        paymentDataRepository.paymentData = action.paymentData
        makeRedirect(activity, action.url)
    }

    private fun makeRedirect(activity: Activity, url: String?) {
        try {
            Logger.d(TAG, "makeRedirect - $url")
            // TODO look into emitting a value to tell observers that a redirect was launched so they can track its
            //  status when the app resumes. Currently we have no way of doing that but we can create something like
            //  PaymentComponentState for actions.
            redirectHandler.launchUriRedirect(activity, url)
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }

    override fun handleIntent(intent: Intent) {
        try {
            val details = redirectHandler.parseRedirectResult(intent.data)
            _detailsFlow.tryEmit(createActionComponentData(details))
        } catch (ex: CheckoutException) {
            _exceptionFlow.tryEmit(ex)
        }
    }

    private fun createActionComponentData(details: JSONObject): ActionComponentData {
        return ActionComponentData(
            details = details,
            paymentData = paymentDataRepository.paymentData,
        )
    }
}
