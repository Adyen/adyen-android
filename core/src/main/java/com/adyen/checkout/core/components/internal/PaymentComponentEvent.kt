/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 16/5/2025.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.error.internal.InternalCheckoutError

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class PaymentComponentEvent {

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Submit(
        val state: BasePaymentComponentState
    ) : PaymentComponentEvent()

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    data class Error(
        val error: InternalCheckoutError
    ) : PaymentComponentEvent()

    data class SecondaryScreen(
        val identifier: String,
    ) : PaymentComponentEvent()
}
