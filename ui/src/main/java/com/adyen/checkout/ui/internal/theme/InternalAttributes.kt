/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 5/12/2025.
 */

package com.adyen.checkout.ui.internal.theme

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Immutable
import com.adyen.checkout.ui.theme.CheckoutAttributes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Immutable
data class InternalAttributes(
    val cornerRadius: Int,
) {

    internal companion object {

        fun from(attributes: CheckoutAttributes) = InternalAttributes(
            cornerRadius = attributes.cornerRadius,
        )
    }
}
