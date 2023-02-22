/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/6/2022.
 */

package com.adyen.checkout.components.core.internal.ui

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.internal.PaymentComponentEvent
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

/**
 * Handles all the logic in payment components
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponentDelegate<
    ComponentStateT : PaymentComponentState<out PaymentMethodDetails>
    > : ComponentDelegate {

    val submitFlow: Flow<ComponentStateT>

    fun getPaymentMethodType(): String

    fun observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<ComponentStateT>) -> Unit
    )

    fun removeObserver()
}
