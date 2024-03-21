/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 21/3/2024.
 */

package com.adyen.checkout.core.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.Amount
import com.adyen.checkout.core.CheckoutCurrency
import com.adyen.checkout.core.exception.CheckoutException

internal const val EMPTY_CURRENCY = "NONE"
internal const val EMPTY_VALUE = -1L

@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
val Amount.isEmpty: Boolean
    get() = currency == EMPTY_CURRENCY || value == EMPTY_VALUE

@get:RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
val Amount.isZero: Boolean
    get() = CheckoutCurrency.isSupported(currency) && value == 0L

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Amount.validate() {
    if (!CheckoutCurrency.isSupported(currency)) {
        throw CheckoutException("Currency code is not valid.")
    }
    if (value < 0) {
        throw CheckoutException("Value cannot be less than 0.")
    }
}
