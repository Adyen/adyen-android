/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 25/3/2026.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class PaymentMethodBehavior(val value: String) {
    /**
     * Indicates that the SDK does not have native component support for this payment method and will handle it
     * through the Instant Payment Component.
     */
    GENERIC("genericComponent"),

    /**
     * Indicates that the SDK has a specific component for this payment method.
     */
    NATIVE("nativeComponent"),
}
