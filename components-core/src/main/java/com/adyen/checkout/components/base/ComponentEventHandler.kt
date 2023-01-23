/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/1/2023.
 */

package com.adyen.checkout.components.base

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ComponentEventHandler<T : PaymentComponentState<*>> {

    /**
     * Do not keep a local references of this scope if you don't need to.
     *
     * If you have to keep any references to [CoroutineScope], use [onCleared] to clear them.
     */
    fun initialize(coroutineScope: CoroutineScope)

    fun onCleared()

    fun onPaymentComponentEvent(event: PaymentComponentEvent<T>)
}
