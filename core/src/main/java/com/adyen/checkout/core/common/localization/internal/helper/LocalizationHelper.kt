/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization.internal.helper

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.core.common.internal.helper.createLocalizedContext
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.common.localization.internal.LocalizationResolver
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun LocalizedComponent(
    locale: Locale,
    localizationProvider: CheckoutLocalizationProvider?,
    content: @Composable () -> Unit,
) {
    val localizedContext = LocalContext.current.createLocalizedContext(locale)
    CompositionLocalProvider(
        LocalLocalizationResolver provides LocalizationResolver(localizationProvider),
        LocalContext provides localizedContext,
        LocalLocale provides locale,
    ) {
        content()
    }
}

private val LocalLocale = staticCompositionLocalOf { Locale.getDefault() }
private val LocalLocalizationResolver: ProvidableCompositionLocal<LocalizationResolver> =
    staticCompositionLocalOf { LocalizationResolver(localizationProvider = null) }
