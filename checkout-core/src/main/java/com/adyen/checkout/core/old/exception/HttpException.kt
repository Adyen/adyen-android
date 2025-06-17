/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.core.old.exception

import com.adyen.checkout.core.old.internal.data.model.ErrorResponseBody

/**
 * Indicates that an internal API call has failed.
 */
class HttpException(
    val code: Int,
    message: String,
    val errorBody: ErrorResponseBody?,
) : CheckoutException(message)
