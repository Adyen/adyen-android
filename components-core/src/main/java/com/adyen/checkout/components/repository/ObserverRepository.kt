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
import com.adyen.checkout.components.PaymentComponentEvent
import com.adyen.checkout.components.PaymentComponentState
import com.adyen.checkout.components.flow.mapToCallbackWithLifeCycle
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class ObserverRepository {

    private var observerJobs: MutableList<Job> = mutableListOf()

    fun <T : PaymentComponentState<out PaymentMethodDetails>> observePaymentComponentEvents(
        stateFlow: Flow<T>,
        exceptionFlow: Flow<CheckoutException>?,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (PaymentComponentEvent<T>) -> Unit,
    ) {
        removeObservers()

        stateFlow.observe(lifecycleOwner, coroutineScope) {
            callback(PaymentComponentEvent.StateChanged(it))
        }

        exceptionFlow?.observe(lifecycleOwner, coroutineScope) {
            callback(PaymentComponentEvent.Error(ComponentError(it)))
        }
    }

    fun observeActionComponentEvents(
        detailsFlow: Flow<ActionComponentData>?,
        exceptionFlow: Flow<CheckoutException>?,
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (ActionComponentEvent) -> Unit,
    ) {
        removeObservers()

        detailsFlow?.observe(lifecycleOwner, coroutineScope) {
            callback(ActionComponentEvent.ActionDetails(it))
        }

        exceptionFlow?.observe(lifecycleOwner, coroutineScope) {
            callback(ActionComponentEvent.Error(ComponentError(it)))
        }
    }

    private fun <T> Flow<T>.observe(
        lifecycleOwner: LifecycleOwner,
        coroutineScope: CoroutineScope,
        callback: (T) -> Unit,
    ) {
        mapToCallbackWithLifeCycle(
            lifecycleOwner = lifecycleOwner,
            coroutineScope = coroutineScope,
            callback = callback
        ).also {
            observerJobs.add(it)
        }
    }

    fun removeObservers() {
        if (observerJobs.isEmpty()) return
        Logger.d(TAG, "cleaning up existing observer")
        observerJobs.forEach { it.cancel() }
        observerJobs.clear()
    }

    companion object {
        private val TAG = LogUtil.getTag()
    }
}
