/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentComponentEvent

// TODO - Some components might not be composable,
//  Move ComposableComponent to PaymentMethod specific component later
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponent<T : BasePaymentComponentState> :
    ComposableComponent,
    EventComponent<PaymentComponentEvent<T>> {

    fun submit()
}
