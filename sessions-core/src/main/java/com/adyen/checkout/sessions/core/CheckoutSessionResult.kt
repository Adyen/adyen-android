/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/11/2022.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.core.old.exception.CheckoutException

/**
 * The result of the API call to fetch a [CheckoutSession].
 */
@Deprecated(
    message = "Deprecated. This will be removed in a future release.",
    level = DeprecationLevel.WARNING,
)
sealed class CheckoutSessionResult {
    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    class Success(val checkoutSession: CheckoutSession) : CheckoutSessionResult()

    @Deprecated(
        message = "Deprecated. This will be removed in a future release.",
        level = DeprecationLevel.WARNING,
    )
    class Error(val exception: CheckoutException) : CheckoutSessionResult()
}
