/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.internal.ui

import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails

// TODO - Some delegates might not be composable,
//  Move ComposableDelegate to PaymentMethod specific delegate later
internal interface PaymentDelegate<T : PaymentComponentState<out PaymentMethodDetails>> :
    ComposableDelegate,
    EventDelegate<T> {

    fun submit()
}
