/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/5/2025.
 */

package com.adyen.checkout.core.common.internal.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.error.CheckoutError
import java.util.IllformedLocaleException
import java.util.Locale

/**
 * Utility class to use [Locale].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object LocaleUtil {

    /**
     * Validates a locale and returns a result containing either the valid locale or an error.
     *
     * We use [Locale.Builder.setLocale] because the [Locale] constructor does not perform any
     * validation, allowing invalid locales like `Locale("español")` or `Locale("de", "HANS")`.
     * The builder throws [IllformedLocaleException] for such cases, giving us reliable validation.
     *
     * @param locale The locale to validate.
     * @return [LocaleValidationResult.Valid] if the locale is valid, [LocaleValidationResult.Invalid] otherwise.
     */
    @JvmStatic
    fun validateLocale(locale: Locale): LocaleValidationResult = runCatching {
        Locale.Builder().setLocale(locale).build()
    }.fold(
        onSuccess = { LocaleValidationResult.Valid(it) },
        onFailure = { cause ->
            LocaleValidationResult.Invalid(
                CheckoutError(
                    code = CheckoutError.ErrorCode.INVALID_LOCALE,
                    message = "Invalid shopper locale: $locale",
                    cause = cause,
                ),
            )
        },
    )

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

/**
 * Result of locale validation.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
sealed class LocaleValidationResult {
    data class Valid(val locale: Locale) : LocaleValidationResult()
    data class Invalid(val error: CheckoutError) : LocaleValidationResult()
}
