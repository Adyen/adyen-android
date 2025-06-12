/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/6/2025.
 */

package com.adyen.checkout.core.internal.analytics

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class CheckoutAttemptIdState {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Available(val checkoutAttemptId: String) : CheckoutAttemptIdState()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object Failed : CheckoutAttemptIdState()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data object NotAvailable : CheckoutAttemptIdState()
}
