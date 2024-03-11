/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/2/2023.
 */

package com.adyen.checkout.dropin.internal.ui.model

import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.dropin.SessionDropInService
import com.adyen.checkout.sessions.core.CheckoutSession

data class SessionDropInResultContractParams internal constructor(
    val checkoutConfiguration: CheckoutConfiguration,
    val checkoutSession: CheckoutSession,
    val serviceClass: Class<out SessionDropInService>,
)
