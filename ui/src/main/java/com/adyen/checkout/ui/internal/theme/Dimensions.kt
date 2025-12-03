/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/7/2025.
 */

package com.adyen.checkout.ui.internal.theme

import androidx.annotation.RestrictTo
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object Dimensions {

    // TODO - Dimensions: Move all spacing dimensions inside Spacing object
    val ExtraSmall = 4.dp

    val Small = 8.dp

    val Medium = 12.dp

    val Large = 16.dp

    val ExtraLarge = 24.dp

    val DoubleExtraLarge = 32.dp

    val TripleExtraLarge = 48.dp

    val MinTouchTarget = 48.dp

    val CornerRadius = 4.dp

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    object LogoSize {
        val small = DpSize(width = 24.dp, height = 16.dp)

        val smallSquare = DpSize(width = 24.dp, height = 24.dp)

        val medium = DpSize(width = 40.dp, height = 26.dp)

        val large = DpSize(width = 80.dp, height = 52.dp)
    }
}
