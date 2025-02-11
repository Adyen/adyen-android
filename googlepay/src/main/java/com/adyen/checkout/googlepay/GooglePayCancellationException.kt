/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 11/2/2025.
 */

package com.adyen.checkout.googlepay

import com.adyen.checkout.core.exception.CancellationException

/**
 * This exception indicates that the payment flow was manually cancelled by the user.
 */
class GooglePayCancellationException(errorMessage: String) : CancellationException(errorMessage)
