/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/4/2025.
 */

package com.adyen.checkout.ui.internal

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.adyen.checkout.ui.theme.AdyenCheckoutTheme
import com.adyen.checkout.ui.theme.AdyenColors
import com.adyen.checkout.ui.theme.AdyenElements
import com.adyen.checkout.ui.theme.AdyenTextStyles

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun AdyenCheckoutTheme(
    theme: AdyenCheckoutTheme = AdyenCheckoutTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides theme.colors,
        LocalTextStyles provides theme.textStyles,
        LocalElements provides theme.elements,
    ) {
        content()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object AdyenCheckoutTheme {

    val colors: AdyenColors
        @Composable @ReadOnlyComposable get() = LocalColors.current

    val textStyles: AdyenTextStyles
        @Composable @ReadOnlyComposable get() = LocalTextStyles.current

    val elements: AdyenElements
        @Composable @ReadOnlyComposable get() = LocalElements.current
}

private val LocalColors = staticCompositionLocalOf { AdyenColors.light() }
private val LocalTextStyles = staticCompositionLocalOf { AdyenTextStyles.default() }
private val LocalElements = staticCompositionLocalOf { AdyenElements.default() }
