/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 18/11/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.components.CheckoutCallback

fun interface OnBinValueCallback : CheckoutCallback {

    fun onBinValue(binValue: String)
}
