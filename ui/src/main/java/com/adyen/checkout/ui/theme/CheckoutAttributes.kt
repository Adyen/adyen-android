/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 23/7/2025.
 */

package com.adyen.checkout.ui.theme

import androidx.compose.runtime.Immutable

@Immutable
data class CheckoutAttributes(
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
