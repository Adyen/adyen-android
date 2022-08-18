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
import com.adyen.checkout.components.model.payments.response.RedirectAction
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface RedirectDelegate {

    val detailsFlow: Flow<JSONObject>

    val exceptionFlow: Flow<CheckoutException>

    fun handleAction(activity: Activity, redirectAction: RedirectAction)

    fun handleIntent(intent: Intent)
}
