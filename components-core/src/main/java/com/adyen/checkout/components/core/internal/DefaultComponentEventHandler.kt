/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/1/2023.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.exception.CheckoutException
import com.adyen.checkout.core.old.internal.util.adyenLog
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultComponentEventHandler<T : PaymentComponentState<*>> : ComponentEventHandler<T> {

    // no ops
    override fun initialize(coroutineScope: CoroutineScope) = Unit

    // no ops
    override fun onCleared() = Unit

    override fun onPaymentComponentEvent(event: PaymentComponentEvent<T>, componentCallback: BaseComponentCallback) {
        @Suppress("UNCHECKED_CAST")
        val callback = componentCallback as? ComponentCallback<T>
            ?: throw CheckoutException("Callback must be type of ${ComponentCallback::class.java.canonicalName}")
        adyenLog(AdyenLogLevel.VERBOSE) { "Event received $event" }
        when (event) {
            is PaymentComponentEvent.ActionDetails -> callback.onAdditionalDetails(event.data)
            is PaymentComponentEvent.Error -> callback.onError(event.error)
            is PaymentComponentEvent.StateChanged -> callback.onStateChanged(event.state)
            is PaymentComponentEvent.Submit -> callback.onSubmit(event.state)
            is PaymentComponentEvent.PermissionRequest -> callback.onPermissionRequest(
                event.requiredPermission,
                event.permissionCallback,
            )
        }
    }
}
