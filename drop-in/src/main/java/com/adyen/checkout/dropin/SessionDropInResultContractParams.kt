/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/2/2023.
 */

package com.adyen.checkout.dropin

import androidx.annotation.RestrictTo
import com.adyen.checkout.dropin.service.SessionDropInService
import com.adyen.checkout.sessions.core.CheckoutSession

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class SessionDropInResultContractParams(
    val dropInConfiguration: DropInConfiguration,
    val checkoutSession: CheckoutSession,
    val serviceClass: Class<out SessionDropInService>,
)
