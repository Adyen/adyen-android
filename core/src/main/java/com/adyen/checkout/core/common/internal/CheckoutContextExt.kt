/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/5/2026.
 */

package com.adyen.checkout.core.common.internal

import com.adyen.checkout.core.common.CheckoutContext

internal val CheckoutContext.checkoutAttemptId: String?
    get() = when (this) {
        is CheckoutContext.Sessions -> checkoutAttemptId
        is CheckoutContext.Advanced -> checkoutAttemptId
        is CheckoutContext.ActionOnly -> checkoutAttemptId
    }

internal val CheckoutContext.publicKey: String?
    get() = when (this) {
        is CheckoutContext.Sessions -> publicKey
        is CheckoutContext.Advanced -> publicKey
        is CheckoutContext.ActionOnly -> publicKey
    }
