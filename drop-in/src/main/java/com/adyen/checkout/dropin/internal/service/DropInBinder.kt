/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/1/2026.
 */

package com.adyen.checkout.dropin.internal.service

import android.os.Binder
import com.adyen.checkout.core.action.data.ActionComponentData
import com.adyen.checkout.core.components.CheckoutResult
import com.adyen.checkout.core.components.paymentmethod.PaymentComponentState

internal abstract class DropInBinder : Binder() {

    abstract suspend fun requestOnSubmit(state: PaymentComponentState<*>): CheckoutResult

    abstract suspend fun requestOnAdditionalDetails(data: ActionComponentData): CheckoutResult
}
