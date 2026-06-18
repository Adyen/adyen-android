/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/6/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.components.internal.ui.PaymentComponent

internal sealed class PaymentComponentResult {

    data class Success(val component: PaymentComponent) : PaymentComponentResult()

    data class Failure(val message: String) : PaymentComponentResult()
}
