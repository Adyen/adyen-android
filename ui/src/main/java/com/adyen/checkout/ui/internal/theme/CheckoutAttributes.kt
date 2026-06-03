/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/7/2025.
 */

package com.adyen.checkout.ui.internal.theme

import androidx.compose.runtime.Immutable

// This class is internal for now, but if we will support customizing this in the future, it should be public.
@Immutable
internal data class CheckoutAttributes(
    val cornerRadius: Int,
) {

    companion object {

        private const val DEFAULT_CORNER_RADIUS = 8

        fun default(
            cornerRadius: Int = DEFAULT_CORNER_RADIUS,
        ) = CheckoutAttributes(
            cornerRadius = cornerRadius,
        )
    }
}
