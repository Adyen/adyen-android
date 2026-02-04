/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2026.
 */

package com.adyen.checkout.core.components.internal

import com.adyen.checkout.core.common.CheckoutCurrency
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.error.CheckoutError

/**
 * Validates the [Amount] and returns a [CheckoutError] if validation fails.
 *
 * @return [CheckoutError] if validation fails, `null` otherwise.
 */
internal fun Amount.validate(): CheckoutError? = when {
    !CheckoutCurrency.isSupported(currency) -> CheckoutError(
        code = CheckoutError.ErrorCode.INVALID_CURRENCY_CODE,
        message = "Invalid currency code: $currency",
    )
    value < 0 -> CheckoutError(
        code = CheckoutError.ErrorCode.INVALID_AMOUNT_VALUE,
        message = "Value cannot be less than 0",
    )
    else -> null
}
