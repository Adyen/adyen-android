/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/10/2022.
 */

package com.adyen.checkout.components

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.payments.request.PaymentMethodDetails

// TODO add docs
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentEvent<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>> : ComponentEvent {
    class StateChanged<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val state: ComponentStateT
    ) : PaymentComponentEvent<ComponentStateT>()

    class Error<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val error: ComponentError
    ) : PaymentComponentEvent<ComponentStateT>()

    class ActionDetails<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val data: ActionComponentData
    ) : PaymentComponentEvent<ComponentStateT>()

    class Submit<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val state: ComponentStateT
    ) : PaymentComponentEvent<ComponentStateT>()
}
