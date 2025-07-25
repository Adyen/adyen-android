/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/5/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.BasePaymentComponentState
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import kotlinx.coroutines.flow.Flow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface EventComponent<T : BasePaymentComponentState> {

    val eventFlow: Flow<PaymentComponentEvent<T>>
}
