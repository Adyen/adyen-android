/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/5/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import com.adyen.checkout.core.components.internal.BaseComponentState
import com.adyen.checkout.core.components.internal.PaymentComponentEvent
import kotlinx.coroutines.flow.Flow

internal interface EventDelegate<T : BaseComponentState> {

    val eventFlow: Flow<PaymentComponentEvent<T>>
}
