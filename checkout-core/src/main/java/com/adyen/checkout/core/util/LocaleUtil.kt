/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.util

import android.content.Context
import android.os.Build
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * Utility class to use [Locale].
 */
object LocaleUtil {

    /**
     * Get the current user Locale.
     * @param context The context
     * @return The user Locale
     */
    @JvmStatic
    fun getLocale(context: Context): Locale {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.resources.configuration.locales[0]
        } else {
            @Suppress("DEPRECATION")
            context.resources.configuration.locale
        }
    }

    /**
     * Gets the language tag from a Locale.
     *
     * @param locale The locale.
     * @return The tag of the locale, or null if not valid.
     */
    @JvmStatic
    fun toLanguageTag(locale: Locale): String {
        return locale.toLanguageTag()
    }

    /**
     * Creates a Locale instance for a specific language tag.
     *
     * @param tag The tag of the language.
     * @return The locale associated with that tag or null if tag in invalid.
     */
    @JvmStatic
    fun fromLanguageTag(tag: String): Locale {
        return Locale.forLanguageTag(tag)
    }

    /**
     * Checks if a locale is valid.
     *
     * @param locale The locale.
     * @return Whether locale is valid or not.
     */
    @JvmStatic
    fun isValidLocale(locale: Locale): Boolean {
        return try {
            Locale.Builder().setLocale(locale).build()
            return true
        } catch (ex: IllformedLocaleException) {
            false
        }
    }
}
