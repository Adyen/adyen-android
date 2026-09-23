/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/9/2026.
 */

package com.adyen.checkout.core.components.internal.data.model.sdkData

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
enum class PaymentMethodBehavior(val value: String) {
    /**
     * Indicates that the SDK does not have native component support for this payment method and will handle it
     * through the generic payment component.
     */
    GENERIC("genericComponent"),

    /**
     * Indicates that the SDK has a specific component for this payment method.
     */
    NATIVE("nativeComponent"),
}
