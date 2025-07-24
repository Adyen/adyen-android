/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CustomTabsLauncher {
    fun launchCustomTab(context: Context, uri: Uri): Boolean {
        @Suppress("SwallowedException")
        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(getDefaultColorSchemeParams())
                .build()
                .launchUrl(context, uri)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun getDefaultColorSchemeParams(): CustomTabColorSchemeParams {
        // TODO - Custom tabs style customization
        return CustomTabColorSchemeParams.Builder().build()
    }
}
