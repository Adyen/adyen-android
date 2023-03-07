/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 14/11/2022.
 */

package com.adyen.checkout.sessions.core

import com.adyen.checkout.core.exception.CheckoutException

// TODO docs
sealed class CheckoutSessionResult {
    // TODO docs
    class Success(val checkoutSession: CheckoutSession) : CheckoutSessionResult()

    // TODO docs
    class Error(val exception: CheckoutException) : CheckoutSessionResult()
}
