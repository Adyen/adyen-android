/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/1/2026.
 */

package com.adyen.checkout.core.error

typealias InternalCheckoutError = com.adyen.checkout.core.common.exception.CheckoutError

// Maps internal error hierarchy to public CheckoutError.
// Add new mappings here as new error types are introduced.
internal fun InternalCheckoutError.toCheckoutError(): CheckoutError {
    return CheckoutError(
        code = CheckoutError.ErrorCode.UNKNOWN,
        message = message,
        cause = this,
    )
}
