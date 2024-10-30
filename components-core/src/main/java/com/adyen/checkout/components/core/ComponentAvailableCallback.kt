/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */
package com.adyen.checkout.components.core

fun interface ComponentAvailableCallback {
    fun onAvailabilityResult(isAvailable: Boolean, paymentMethod: PaymentMethod)
}
