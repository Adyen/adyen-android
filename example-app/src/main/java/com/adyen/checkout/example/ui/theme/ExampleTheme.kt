/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2023.
 */

package com.adyen.checkout.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

@Composable
fun ExampleTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        LightColors
    } else {
        DarkColors
    }

    val customColors = if (!useDarkTheme) {
        CustomLightColors
    } else {
        CustomDarkColors
    }

    val dimensions = DefaultDimensions

    CompositionLocalProvider(
        LocalCustomColorScheme provides customColors,
        LocalDimensions provides dimensions,
    ) {
        MaterialTheme(
            colorScheme = colors,
            content = content,
        )
    }
}

object ExampleTheme {

    val customColors: CustomColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCustomColorScheme.current

    val dimensions: Dimensions
        @Composable
        @ReadOnlyComposable
        get() = LocalDimensions.current
}

private val LocalDimensions = staticCompositionLocalOf { Dimensions() }
private val LocalCustomColorScheme = staticCompositionLocalOf { CustomColorScheme() }
