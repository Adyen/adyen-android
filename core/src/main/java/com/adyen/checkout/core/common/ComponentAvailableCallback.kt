/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.core.common

import com.adyen.checkout.core.components.data.model.PaymentMethod

fun interface ComponentAvailableCallback {
    fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod)
}
