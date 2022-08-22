/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 19/8/2022.
 */

package com.adyen.checkout.wechatpay

import android.content.Intent
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface WeChatDelegate {

    val detailsFlow: Flow<JSONObject>

    val exceptionFlow: Flow<CheckoutException>

    fun handleIntent(intent: Intent)

    fun handleAction(action: Action, activityName: String)
}
