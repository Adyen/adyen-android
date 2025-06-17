/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/2/2023.
 */

package com.adyen.checkout.core.old.exception

/**
 * Indicates that a required method was not implemented.
 */
class MethodNotImplementedException(
    errorMessage: String,
    cause: Throwable? = null
) : CheckoutException(errorMessage, cause)
