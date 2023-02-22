/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 30/1/2023.
 */

package com.adyen.checkout.example.ui.instant

import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.paymentmethod.PaymentMethodDetails

internal data class InstantComponentData(
    val paymentMethod: PaymentMethod,
    val callback: ComponentCallback<PaymentComponentState<PaymentMethodDetails>>,
)
