/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 24/8/2022.
 */

package com.adyen.checkout.components.base

import android.app.Activity
import com.adyen.checkout.components.model.payments.response.Action
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

interface ActionDelegate<ActionT : Action> {

    val exceptionFlow: Flow<CheckoutException>

    fun handleAction(action: ActionT, activity: Activity)

    /**
     * Override this method if you need to initialize your delegate and use a [CoroutineScope] inside it.
     */
    fun initialize(coroutineScope: CoroutineScope) = Unit

    /**
     * Override this method if you used [initialize] to clear any local reference to [CoroutineScope].
     */
    fun onCleared() = Unit
}
