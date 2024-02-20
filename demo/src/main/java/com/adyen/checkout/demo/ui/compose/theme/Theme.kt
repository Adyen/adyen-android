/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 20/2/2024.
 */

package com.adyen.checkout.demo.ui.compose.theme

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
