/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/10/2024.
 */

package com.adyen.checkout.components.core

import com.adyen.checkout.core.old.exception.CheckoutException

open class PaymentMethodUnavailableException(
    message: String,
    cause: Throwable? = null
) : CheckoutException(message, cause)
