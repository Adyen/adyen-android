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
import androidx.annotation.AttrRes
import androidx.annotation.RestrictTo
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import com.adyen.checkout.ui.core.R

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CustomTabsLauncher {
    fun launchCustomTab(context: Context, uri: Uri): Boolean {
        @Suppress("SwallowedException")
        return try {
            CustomTabsIntent.Builder()
                .setShowTitle(true)
                .setDefaultColorSchemeParams(getDefaultColorSchemeParams(context))
                .build()
                .launchUrl(context, uri)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    private fun getDefaultColorSchemeParams(context: Context): CustomTabColorSchemeParams {
        val toolbarColor = context.getColorOrNull(R.attr.adyenCustomTabsToolbarColor)
        val secondaryToolbarColor = context.getColorOrNull(R.attr.adyenCustomTabsSecondaryToolbarColor)
        val navigationBarColor = context.getColorOrNull(R.attr.adyenCustomTabsNavigationBarColor)
        val navigationBarDividerColor = context.getColorOrNull(R.attr.adyenCustomTabsNavigationBarDividerColor)

        return CustomTabColorSchemeParams.Builder().apply {
            toolbarColor?.let { setToolbarColor(it) }
            secondaryToolbarColor?.let { setSecondaryToolbarColor(it) }
            navigationBarColor?.let { setNavigationBarColor(it) }
            navigationBarDividerColor?.let { setNavigationBarDividerColor(it) }
        }.build()
    }

    private fun Context.getColorOrNull(@AttrRes attribute: Int): Int? {
        val typedArray = obtainStyledAttributes(R.style.AdyenCheckout_CustomTabs, intArrayOf(attribute))
        val color = typedArray.getColor(0, -1).takeIf { it != -1 }
        typedArray.recycle()
        return color
    }
}
