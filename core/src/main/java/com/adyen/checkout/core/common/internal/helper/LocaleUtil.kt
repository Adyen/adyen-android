/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import com.adyen.checkout.core.error.CheckoutError
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * Utility class to use [Locale].
 */
internal object LocaleUtil {

    /**
     * Validates a locale and returns an error if invalid.
     *
     * We use [Locale.Builder.setLocale] because the [Locale] constructor does not perform any
     * validation, allowing invalid locales like `Locale("español")` or `Locale("de", "HANS")`.
     * The builder throws [IllformedLocaleException] for such cases, giving us reliable validation.
     *
     * @param locale The locale to validate.
     * @return [CheckoutError] if the locale is invalid, `null` otherwise.
     */
    fun validateLocale(locale: Locale): CheckoutError? = try {
        Locale.Builder().setLocale(locale).build()
        null
    } catch (e: IllformedLocaleException) {
        CheckoutError(
            code = CheckoutError.ErrorCode.INVALID_LOCALE,
            message = "Invalid shopper locale: $locale",
            cause = e,
        )
    }

    /**
     * Creates a Locale instance for a specific language tag.
     *
     * @param tag The tag of the language.
     * @return The locale associated with that tag or null if tag in invalid.
     */
    fun fromLanguageTag(tag: String): Locale {
        return Locale.forLanguageTag(tag)
    }
}
