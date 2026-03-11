/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/1/2026.
 */

package com.adyen.checkout.core.error

import com.adyen.checkout.core.error.internal.GenericError
import com.adyen.checkout.core.error.internal.HttpError
import com.adyen.checkout.core.error.internal.InternalCheckoutError

/**
 * Maps internal error hierarchy to public CheckoutError.
 */
internal fun InternalCheckoutError.toCheckoutError(): CheckoutError {
    val errorCode = when (this) {
        is HttpError -> CheckoutError.ErrorCode.HTTP
        is GenericError -> CheckoutError.ErrorCode.UNKNOWN
    }

    return CheckoutError(
        code = errorCode,
        message = message,
        cause = this,
    )
}
