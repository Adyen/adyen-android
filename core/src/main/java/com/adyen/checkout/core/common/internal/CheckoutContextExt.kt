/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/5/2026.
 */

// These extensions intentionally share their names with the properties they read. The subtypes declare those properties
// as internal members, and they cannot be pulled up into CheckoutContext because members of an interface are always
// public. Inside each branch below the receiver is smart cast to the subtype, and a member always takes precedence over
// an extension, so each getter reads the member rather than itself. Note that this only holds while the members exist:
// removing or renaming one would make the branch resolve to this extension instead, which the compiler accepts and
// which recurses until the stack overflows.
@file:Suppress("MemberExtensionConflict")

package com.adyen.checkout.core.common.internal

import com.adyen.checkout.core.common.CheckoutContext

internal val CheckoutContext.checkoutAttemptId: String
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
