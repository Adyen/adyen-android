/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 16/8/2022.
 */

package com.adyen.checkout.await

import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import org.json.JSONObject

interface AwaitDelegate {

    val outputDataFlow: Flow<AwaitOutputData?>

    val outputData: AwaitOutputData?

    val detailsFlow: Flow<JSONObject?>

    val exceptionFlow: Flow<CheckoutException>

    fun initialize(coroutineScope: CoroutineScope)

    fun handleAction(action: Action, paymentData: String)

    fun refreshStatus(paymentData: String)

    fun onCleared()
}
