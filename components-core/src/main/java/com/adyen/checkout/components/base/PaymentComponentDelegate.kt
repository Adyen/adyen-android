/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.components.base

import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope

/**
 * Handles all the logic in payment components
 */
interface PaymentComponentDelegate<
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>
    > : ComponentDelegate {

    fun getPaymentMethodType(): String

    fun initialize(coroutineScope: CoroutineScope)

    fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<ComponentStateT>) -> Unit
    )

    fun removeObserver()
}
