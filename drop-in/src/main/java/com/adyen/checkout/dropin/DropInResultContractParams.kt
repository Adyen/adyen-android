/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/2/2023.
 */

package com.adyen.checkout.dropin

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.model.PaymentMethodsApiResponse
import com.adyen.checkout.dropin.service.DropInService

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DropInResultContractParams(
    val dropInConfiguration: DropInConfiguration,
    val paymentMethodsApiResponse: PaymentMethodsApiResponse,
    val serviceClass: Class<out DropInService>,
)
