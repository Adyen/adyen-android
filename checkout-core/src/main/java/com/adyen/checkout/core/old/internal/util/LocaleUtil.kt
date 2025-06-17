/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */
package com.adyen.checkout.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.old.exception.CheckoutException
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * Utility class to use [Locale].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object LocaleUtil {

    /**
     * Gets the language tag from a Locale.
     *
     * @param locale The locale.
     * @return The tag of the locale, or null if not valid.
     */
    @JvmStatic
    fun toLanguageTag(locale: Locale): String {
        if (!isValidLocale(locale)) {
            throw CheckoutException("Invalid shopper locale: $locale.")
        }
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
        @Suppress("SwallowedException")
        return try {
            Locale.Builder().setLocale(locale).build()
            true
        } catch (ex: IllformedLocaleException) {
            false
        }
    }
}
