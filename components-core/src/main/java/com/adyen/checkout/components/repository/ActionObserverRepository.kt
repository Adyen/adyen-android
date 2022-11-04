/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 3/11/2022.
 */

package com.adyen.checkout.components.repository

import androidx.annotation.RestrictTo
import androidx.lifecycle.LifecycleOwner
import com.adyen.checkout.components.ActionComponentData
import com.adyen.checkout.components.ActionComponentEvent
import com.adyen.checkout.components.ComponentError
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ActionObserverRepository(
    private val observerContainer: ObserverContainer = ObserverContainer()
) {

    fun addObservers(
        detailsFlow: Flow<ActionComponentData>?,
        exceptionFlow: Flow<CheckoutException>?,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit,
    ) {
        with(observerContainer) {
            removeObservers()

            detailsFlow?.observe(lifecycleOwner, coroutineScope) {
                callback(ActionComponentEvent.ActionDetails(it))
            }

            exceptionFlow?.observe(lifecycleOwner, coroutineScope) {
                callback(ActionComponentEvent.Error(ComponentError(it)))
            }
        }
    }

    fun removeObservers() {
        observerContainer.removeObservers()
    }
}
