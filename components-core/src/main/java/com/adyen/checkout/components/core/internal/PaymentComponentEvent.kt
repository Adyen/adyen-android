/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/10/2022.
 */

package com.adyen.checkout.components.core.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.ActionComponentData
import com.adyen.checkout.components.core.ComponentError
import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails
import com.adyen.checkout.core.PermissionHandlerCallback

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

    class PermissionRequest<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val requiredPermission: String,
        val permissionCallback: PermissionHandlerCallback
    ) : PaymentComponentEvent<ComponentStateT>()

    class Submit<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val state: ComponentStateT
    ) : PaymentComponentEvent<ComponentStateT>()

    class AvailabilityResult<ComponentStateT : PaymentComponentState<out PaymentMethodDetails>>(
        val isAvailable: Boolean,
    ) : PaymentComponentEvent<ComponentStateT>()
}
