/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 13/6/2025.
 */

package com.adyen.checkout.core.internal

import com.adyen.checkout.core.CheckoutConfiguration
import com.adyen.checkout.core.internal.ui.PaymentDelegate
import com.adyen.checkout.core.sessions.CheckoutSession
import kotlinx.coroutines.CoroutineScope

internal interface PaymentMethodFactory<CS : BaseComponentState, T : PaymentDelegate<CS>> {

    /**
     * Creates an Advanced Payment Method Delegate.
     */
    fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration
    ): T

    /**
     * Creates a Sessions Payment Method Delegate.
     */
    fun create(
        coroutineScope: CoroutineScope,
        checkoutSession: CheckoutSession,
        checkoutConfiguration: CheckoutConfiguration
    ): T
}
