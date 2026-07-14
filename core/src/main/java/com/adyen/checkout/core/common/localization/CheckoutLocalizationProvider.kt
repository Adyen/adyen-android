/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/9/2025.
 */

package com.adyen.checkout.core.common.localization

import android.content.Context
import java.util.Locale

/**
 * Supplies the localized text shown by the checkout UI, allowing you to override the default copy.
 *
 * Provide your own implementation to fully control the strings displayed to the shopper, or use
 * [StringResourceLocalizationProvider] to map keys to Android string resources.
 */
interface CheckoutLocalizationProvider {
    /**
     * Returns the localized string for the given [key], or `null` to fall back to the default text.
     *
     * @param context A [Context] used to resolve string resources.
     * @param locale The shopper [Locale] the text should be localized for.
     * @param key The [CheckoutLocalizationKey] identifying the text to resolve.
     * @return The localized string, or `null` if no override is provided for this key.
     */
    fun getLocalizedString(context: Context, locale: Locale, key: CheckoutLocalizationKey): String?
}

/**
 * A [CheckoutLocalizationProvider] that resolves each [CheckoutLocalizationKey] to an Android string
 * resource.
 *
 * @param localizations A map from localization keys to their corresponding string resource IDs.
 * Keys that are not present in the map fall back to the default text.
 */
class StringResourceLocalizationProvider(
    private val localizations: Map<CheckoutLocalizationKey, Int>,
) : CheckoutLocalizationProvider {
    override fun getLocalizedString(
        context: Context,
        locale: Locale,
        key: CheckoutLocalizationKey,
    ): String? {
        val resourceId = localizations[key]
        return resourceId?.let {
            context.getString(resourceId)
        }
    }
}
