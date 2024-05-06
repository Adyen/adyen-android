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
import android.content.res.TypedArray
import android.net.Uri
import androidx.annotation.RestrictTo
import androidx.annotation.StyleableRes
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
        val typedArray = context.obtainStyledAttributes(
            R.style.AdyenCheckout_CustomTabs,
            R.styleable.AdyenCheckoutCustomTabs,
        )
        val toolbarColor = typedArray.getColorOrNull(
            R.styleable.AdyenCheckoutCustomTabs_adyenCustomTabsToolbarColor,
        )
        val secondaryToolbarColor = typedArray.getColorOrNull(
            R.styleable.AdyenCheckoutCustomTabs_adyenCustomTabsSecondaryToolbarColor,
        )
        val navigationBarColor = typedArray.getColorOrNull(
            R.styleable.AdyenCheckoutCustomTabs_adyenCustomTabsNavigationBarColor,
        )
        val navigationBarDividerColor = typedArray.getColorOrNull(
            R.styleable.AdyenCheckoutCustomTabs_adyenCustomTabsNavigationBarDividerColor,
        )
        typedArray.recycle()

        return CustomTabColorSchemeParams.Builder().apply {
            toolbarColor?.let { setToolbarColor(it) }
            secondaryToolbarColor?.let { setSecondaryToolbarColor(it) }
            navigationBarColor?.let { setNavigationBarColor(it) }
            navigationBarDividerColor?.let { setNavigationBarDividerColor(it) }
        }.build()
    }

    private fun TypedArray.getColorOrNull(@StyleableRes index: Int): Int? {
        return getColor(index, COLOR_NOT_DEFINED).takeIf { it != COLOR_NOT_DEFINED }
    }

    private const val COLOR_NOT_DEFINED = -1
}
