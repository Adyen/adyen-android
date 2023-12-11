/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/12/2023.
 */

package com.adyen.checkout.example.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Suppress("ConstructorParameterNaming")
@Immutable
data class Dimensions(
    val grid_0_25: Dp = Dp.Unspecified,
    val grid_0_5: Dp = Dp.Unspecified,
    val grid_1: Dp = Dp.Unspecified,
    val grid_1_5: Dp = Dp.Unspecified,
    val grid_2: Dp = Dp.Unspecified,
    val grid_4: Dp = Dp.Unspecified,
    val grid_8: Dp = Dp.Unspecified,
) {

    constructor(gridSize: Int) : this(
        grid_0_25 = (gridSize * 0.25).dp,
        grid_0_5 = (gridSize * 0.5).dp,
        grid_1 = gridSize.dp,
        grid_1_5 = (gridSize * 1.5).dp,
        grid_2 = (gridSize * 2).dp,
        grid_4 = (gridSize * 4).dp,
        grid_8 = (gridSize * 8).dp,
    )
}

val DefaultDimensions = Dimensions(gridSize = 8)
