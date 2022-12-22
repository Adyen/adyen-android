package com.adyen.checkout.components.util

import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.core.exception.CheckoutException

internal const val EMPTY_CURRENCY = "NONE"
internal const val EMPTY_VALUE = -1L

val Amount.isEmpty: Boolean
    get() = currency == EMPTY_CURRENCY || value == EMPTY_VALUE

val Amount.isZero: Boolean
    get() = CheckoutCurrency.isSupported(currency) && value == 0L

fun Amount.validate() {
    if (!isEmpty) {
        if (!CheckoutCurrency.isSupported(currency)) {
            throw CheckoutException("Currency code is not valid.")
        }
        if (value < 0) {
            throw CheckoutException("Value cannot be less than 0.")
        }
    }
}
