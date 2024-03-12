/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/2/2023.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.DropInService

data class DropInResultContractParams internal constructor(
    val checkoutConfiguration: CheckoutConfiguration,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    val serviceClass: Class<out DropInService>,
)
