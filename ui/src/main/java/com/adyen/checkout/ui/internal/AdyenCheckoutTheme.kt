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
import com.adyen.checkout.ui.theme.CheckoutTheme
import com.adyen.checkout.ui.theme.CheckoutColors
import com.adyen.checkout.ui.theme.AdyenElements
import com.adyen.checkout.ui.theme.AdyenTextStyles
import com.adyen.checkout.ui.theme.CheckoutAttributes

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun AdyenCheckoutTheme(
    theme: CheckoutTheme = CheckoutTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides InternalColors.from(theme.colors),
        LocalTextStyles provides theme.textStyles,
        LocalAttributes provides theme.attributes,
        LocalElements provides theme.elements,
    ) {
        content()
    }
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object AdyenCheckoutTheme {

    val colors: InternalColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val textStyles: AdyenTextStyles
        @Composable
        @ReadOnlyComposable
        get() = LocalTextStyles.current

    val attributes: CheckoutAttributes
        @Composable
        @ReadOnlyComposable
        get() = LocalAttributes.current

    val elements: AdyenElements
        @Composable
        @ReadOnlyComposable
        get() = LocalElements.current
}

private val LocalColors = staticCompositionLocalOf { InternalColors.from(CheckoutColors.light()) }
private val LocalTextStyles = staticCompositionLocalOf { AdyenTextStyles.default() }
private val LocalAttributes = staticCompositionLocalOf { CheckoutAttributes.default() }
private val LocalElements = staticCompositionLocalOf { AdyenElements.default() }
