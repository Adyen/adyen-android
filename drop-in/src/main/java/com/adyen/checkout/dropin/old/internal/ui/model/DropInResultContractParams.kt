/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/11/2025.
 */

package com.adyen.checkout.dropin.old.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.old.DropInService

data class DropInResultContractParams internal constructor(
    val checkoutConfiguration: CheckoutConfiguration,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    val serviceClass: Class<out DropInService>,
)
