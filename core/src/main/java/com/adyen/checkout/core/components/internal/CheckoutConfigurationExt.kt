/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 2/9/2026.
 */

package com.adyen.checkout.core.components.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.CheckoutConfiguration

/**
 * Creates a copy of this configuration with a different [showSubmitButton].
 *
 * Every other value is carried over unchanged, including the configurations that were added through the
 * configuration block. The generated `copy` of [CheckoutConfiguration] is internal to this module, which is why other
 * modules go through this function.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun CheckoutConfiguration.withShowSubmitButton(showSubmitButton: Boolean?): CheckoutConfiguration {
    return copy(showSubmitButton = showSubmitButton)
}
