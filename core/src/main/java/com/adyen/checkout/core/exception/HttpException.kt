/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.exception

import com.adyen.checkout.core.internal.data.model.ErrorResponseBody

/**
 * Indicates that an internal API call has failed.
 */
class HttpException(
    val code: Int,
    message: String,
    val errorBody: ErrorResponseBody?,
    // TODO - Errors
// ) : CheckoutException(message)
) : RuntimeException(message)
