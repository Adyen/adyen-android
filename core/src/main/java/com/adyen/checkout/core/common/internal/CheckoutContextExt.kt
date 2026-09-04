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

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.components.Checkout
import com.adyen.checkout.core.components.CheckoutConfiguration

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

/**
 * Creates a copy of this context with a different [checkoutConfiguration], keeping the subtype and all of its other
 * values.
 *
 * The copy of each subtype is internal, since a [CheckoutContext] is only ever meant to come out of [Checkout.setup].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun CheckoutContext.withCheckoutConfiguration(checkoutConfiguration: CheckoutConfiguration): CheckoutContext {
    return when (this) {
        is CheckoutContext.Advanced -> copy(checkoutConfiguration = checkoutConfiguration)
        is CheckoutContext.Sessions -> copy(checkoutConfiguration = checkoutConfiguration)
        is CheckoutContext.ActionOnly -> copy(checkoutConfiguration = checkoutConfiguration)
    }
}
