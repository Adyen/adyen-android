/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/1/2023.
 */

package com.adyen.checkout.example.ui.blik

import com.adyen.checkout.components.core.PaymentComponentState
import com.adyen.checkout.components.core.internal.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.paymentmethod.BlikPaymentMethod

data class BlikComponentData(
    val paymentMethod: PaymentMethod,
    val callback: ComponentCallback<PaymentComponentState<BlikPaymentMethod>>,
)
