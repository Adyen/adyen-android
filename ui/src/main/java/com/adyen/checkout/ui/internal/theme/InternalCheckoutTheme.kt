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
import androidx.compose.runtime.staticCompositionLocalOf
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
    CompositionLocalProvider(
        LocalColors provides InternalColors.from(theme.colors),
        LocalTextStyles provides InternalTextStyles.from(theme.textStyles),
        LocalAttributes provides theme.attributes,
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

    val attributes: CheckoutAttributes
        @Composable
        @ReadOnlyComposable
        get() = LocalAttributes.current
}

private val LocalColors = staticCompositionLocalOf { InternalColors.from(CheckoutColors.light()) }
private val LocalTextStyles = staticCompositionLocalOf { InternalTextStyles.from(CheckoutTextStyles.default()) }
private val LocalAttributes = staticCompositionLocalOf { CheckoutAttributes.default() }
