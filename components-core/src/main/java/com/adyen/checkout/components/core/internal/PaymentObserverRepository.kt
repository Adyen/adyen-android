/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/11/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.old.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class PaymentObserverRepository(
    private val observerContainer: ObserverContainer = ObserverContainer()
) {

    @Suppress("LongParameterList")
    fun <T : PaymentComponentState<out PaymentMethodDetails>> addObservers(
        stateFlow: Flow<T>,
        exceptionFlow: Flow<CheckoutException>?,
        submitFlow: Flow<T>,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<T>) -> Unit,
    ) {
        with(observerContainer) {
            removeObservers()

            stateFlow.observe(lifecycleOwner, coroutineScope) {
                callback(PaymentComponentEvent.StateChanged(it))
            }

            exceptionFlow?.observe(lifecycleOwner, coroutineScope) {
                callback(PaymentComponentEvent.Error(ComponentError(it)))
            }

            submitFlow.observe(lifecycleOwner, coroutineScope) {
                callback(PaymentComponentEvent.Submit(it))
            }
        }
    }

    fun removeObservers() {
        observerContainer.removeObservers()
    }
}
