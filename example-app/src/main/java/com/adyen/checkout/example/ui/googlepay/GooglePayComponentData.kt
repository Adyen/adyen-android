/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2023.
 */

package com.adyen.checkout.example.ui.googlepay

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.ComponentCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.googlepay.old.GooglePayComponentState

internal data class GooglePayComponentData(
    val paymentMethod: PaymentMethod,
    val checkoutConfiguration: CheckoutConfiguration,
    val callback: ComponentCallback<GooglePayComponentState>,
)
