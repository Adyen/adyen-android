/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.PermissionRequestData
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class ActionComponentEvent : ComponentEvent {
    class ActionDetails(val data: ActionComponentData) : ActionComponentEvent()
    class PermissionRequest(val permissionRequestData: PermissionRequestData) : ActionComponentEvent()
    class Error(val error: ComponentError) : ActionComponentEvent()
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <T> ((PaymentComponentEvent<T>) -> Unit).toActionCallback(): (ActionComponentEvent) -> Unit
    where T : PaymentComponentState<out PaymentMethodDetails> {
    return { actionComponentEvent: ActionComponentEvent ->
        when (actionComponentEvent) {
            is ActionComponentEvent.ActionDetails -> {
                this(PaymentComponentEvent.ActionDetails(actionComponentEvent.data))
            }

            is ActionComponentEvent.Error -> {
                this(PaymentComponentEvent.Error(actionComponentEvent.error))
            }

            else -> {
                // Not necessary
            }
        }
    }
}
