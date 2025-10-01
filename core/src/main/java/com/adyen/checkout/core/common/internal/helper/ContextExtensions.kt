/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 28/8/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.annotation.RestrictTo
import java.util.Locale

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.createLocalizedContext(locale: Locale): Context {
    val configuration = resources.configuration
    val newConfig = Configuration(configuration)
    newConfig.setLocale(locale)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        val localeList = LocaleList(locale)
        newConfig.setLocales(localeList)
    }

    return createConfigurationContext(newConfig) ?: this
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun Context.getLocale(): Locale {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        resources.configuration.locales.takeIf { !it.isEmpty }?.get(0)
    } else {
        @Suppress("DEPRECATION")
        resources.configuration.locale
    } ?: Locale.getDefault()
}
