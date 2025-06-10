/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/5/2025.
 */

package com.adyen.checkout.core.internal.ui

import com.adyen.checkout.core.internal.PaymentComponentEvent
import com.adyen.checkout.core.paymentmethod.PaymentComponentState
import com.adyen.checkout.core.paymentmethod.PaymentMethodDetails
import kotlinx.coroutines.flow.Flow

internal interface EventDelegate<T : PaymentComponentState<out PaymentMethodDetails>> {

    val eventFlow: Flow<PaymentComponentEvent<T>>
}
