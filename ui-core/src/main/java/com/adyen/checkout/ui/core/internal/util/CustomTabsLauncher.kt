/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 6/5/2024.
 */

package com.adyen.checkout.ui.core.internal.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CustomTabsLauncher {
    fun launchCustomTab(context: Context, uri: Uri): Boolean {
        val defaultColors = CustomTabColorSchemeParams.Builder()
            .setToolbarColor(ThemeUtil.getPrimaryThemeColor(context))
            .build()

        @Suppress("SwallowedException")
        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(defaultColors)
                .build()
                .launchUrl(context, uri)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
