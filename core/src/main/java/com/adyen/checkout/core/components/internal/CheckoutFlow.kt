/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/4/2026.
 */

package com.adyen.checkout.core.components.internal

import android.content.Intent
import com.adyen.checkout.core.action.internal.ActionComponent
import com.adyen.checkout.core.components.internal.ui.PaymentComponent

internal interface CheckoutFlow {

    val paymentComponent: PaymentComponent?

    val actionComponent: ActionComponent?

    fun submit()

    fun requiresUserInteraction(): Boolean

    fun handleReturn(intent: Intent)
}
