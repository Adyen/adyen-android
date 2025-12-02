/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/11/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import android.content.Context
import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.localization.CheckoutLocalizationProvider
import com.adyen.checkout.core.common.localization.internal.LocalizationResolver
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
@Composable
fun CheckoutCompositionLocalProvider(
    locale: Locale,
    localizationProvider: CheckoutLocalizationProvider?,
    environment: Environment,
    content: @Composable () -> Unit,
) {
    val localizedContext = LocalContext.current.createLocalizedContext(locale)
    CompositionLocalProvider(
        LocalLocalizationResolver provides LocalizationResolver(localizationProvider),
        LocalLocalizedContext provides localizedContext,
        LocalLocale provides locale,
        LocalEnvironment provides environment,
    ) {
        content()
    }
}

internal val LocalEnvironment = staticCompositionLocalOf<Environment> {
    error("No environment provided")
}

internal val LocalLocale = staticCompositionLocalOf<Locale> { error("No locale provided") }

internal val LocalLocalizationResolver =
    staticCompositionLocalOf<LocalizationResolver> { error("No localizationProvider provided") }

internal val LocalLocalizedContext = staticCompositionLocalOf<Context> { error("No default Localized Context.") }
