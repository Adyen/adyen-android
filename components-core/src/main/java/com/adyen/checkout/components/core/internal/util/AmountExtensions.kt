package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.CheckoutCurrency
import com.adyen.checkout.core.old.exception.CheckoutException

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
