/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core.internal.util

import androidx.annotation.RestrictTo
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * Utility class to use [Locale].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object LocaleUtil {

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
}
