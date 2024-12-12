/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/10/2024.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.components.core.PaymentMethodUnavailableException

class GooglePayUnavailableException(
    cause: Throwable? = null,
) : PaymentMethodUnavailableException(
    "Google Pay is not available",
    cause,
)
