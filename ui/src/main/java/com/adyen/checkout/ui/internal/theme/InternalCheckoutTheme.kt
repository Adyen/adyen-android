/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/11/2025.
 */

package com.adyen.checkout.ui.internal.theme

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import com.adyen.checkout.ui.internal.element.InternalElements
import com.adyen.checkout.ui.internal.text.InternalTextStyles
import com.adyen.checkout.ui.theme.CheckoutAttributes
import com.adyen.checkout.ui.theme.CheckoutColors
import com.adyen.checkout.ui.theme.CheckoutTextStyles
import com.adyen.checkout.ui.theme.CheckoutTheme

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun InternalCheckoutTheme(
    theme: CheckoutTheme = CheckoutTheme(),
    content: @Composable () -> Unit,
) {
    val colors = remember(theme.colors) { InternalColors.from(theme.colors) }
    val textStyles = remember(theme.textStyles) { InternalTextStyles.from(theme.textStyles) }
    val attributes = remember(theme.attributes) { InternalAttributes.from(CheckoutAttributes.default()) }
    val elements = remember(colors, attributes) { InternalElements.from(colors, attributes) }
    CompositionLocalProvider(
        LocalColors provides colors,
        LocalTextStyles provides textStyles,
        LocalAttributes provides attributes,
        LocalElements provides elements,
    ) {
        content()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CheckoutThemeProvider {

    val colors: InternalColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val textStyles: InternalTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalTextStyles.current

    val attributes: InternalAttributes
        @Composable
        @ReadOnlyComposable
        get() = LocalAttributes.current

    val elements: InternalElements
        @Composable
        @ReadOnlyComposable
        get() = LocalElements.current
}

// These providers have default values, so previews can display without needing to be wrapped in our theme.
private val LocalColors = staticCompositionLocalOf { InternalColors.from(CheckoutColors.light()) }
private val LocalTextStyles = staticCompositionLocalOf { InternalTextStyles.from(CheckoutTextStyles.default()) }
private val LocalAttributes = staticCompositionLocalOf { InternalAttributes.from(CheckoutAttributes.default()) }
private val LocalElements = staticCompositionLocalOf {
    val colors = InternalColors.from(CheckoutColors.light())
    val attributes = InternalAttributes.from(CheckoutAttributes.default())
    InternalElements.from(colors, attributes)
}
