/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/3/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.data.model.paymentmethod.PaymentMethodResponse
import com.adyen.checkout.core.action.data.Action as ActionResponse

internal sealed interface CheckoutControllerState {
    data class PaymentMethod(val paymentMethod: PaymentMethodResponse?) : CheckoutControllerState
    data class Action(val action: ActionResponse) : CheckoutControllerState
}
