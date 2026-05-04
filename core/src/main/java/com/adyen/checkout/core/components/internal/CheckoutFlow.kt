/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.components.CheckoutPaymentMethodRoute
import com.adyen.checkout.core.components.CheckoutSecondaryRoute
import com.adyen.checkout.core.components.internal.ui.PaymentComponent
import kotlinx.coroutines.flow.Flow

internal interface CheckoutFlow {

    val paymentComponent: PaymentComponent?

    val actionComponent: ActionComponent?

    val paymentMethodNavigation: Flow<CheckoutPaymentMethodRoute>

    val secondaryNavigation: Flow<CheckoutSecondaryRoute>

    fun submit()
}
