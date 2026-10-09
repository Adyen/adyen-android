/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 9/10/2026.
 */

package com.adyen.checkout.address.internal.ui.properties

/**
 * The postal-code format and maximum length per country, ported from Web
 * (`adyen-web/packages/lib/src/components/internal/Address/validate.ts` and `validate.formats.ts`).
 *
 * A country without a pattern accepts any non-blank postal code. Web has a maximum length but no pattern for NZ; that
 * asymmetry is kept on purpose.
 */
internal object PostalCodeProperties {

    /** The API does not accept a longer postal code, whatever the country. */
    private const val POSTAL_CODE_MAX_LENGTH = 10

    // The patterns are matched unanchored, like Web, which is what makes several of them accept documented variants,
    // such as SI-1234 and SK-12345. Do not anchor them, and keep the ^ and $ some of them already have.
    private val PATTERNS: Map<String, Regex> = mapOf(
        "AT" to digits(4),
        "AU" to digits(4),
        "BE" to pattern("""(?:(?:[1-9])(?:\d{3}))"""),
        "BG" to digits(4),
        "BR" to pattern("""^\d{5}-?\d{3}$"""),
        "CA" to pattern("""(?:[ABCEGHJ-NPRSTVXY]\d[A-Z][ -]?\d[A-Z]\d)"""),
        "CH" to pattern("""[1-9]\d{3}"""),
        "CY" to digits(4),
        "CZ" to pattern("""\d{3}\s?\d{2}"""),
        "DE" to digits(5),
        "DK" to digits(4),
        "EE" to digits(5),
        "ES" to pattern("""(?:0[1-9]|[1-4]\d|5[0-2])\d{3}"""),
        "FI" to digits(5),
        "FR" to digits(5),
        "GB" to pattern("""^([A-Za-z][A-Ha-hK-Yk-y]?[0-9][A-Za-z0-9]? ?[0-9][A-Za-z]{2}|[Gg][Ii][Rr] ?0[Aa]{2})$"""),
        "GE" to digits(4),
        "GR" to pattern("""^\d{3}\s{0,1}\d{2}$"""),
        "HR" to pattern("""^([1-5])[0-9]{4}$"""),
        "HU" to digits(4),
        "IE" to pattern("""(?:^[AC-FHKNPRTV-Y][0-9]{2}|D6W)[ -]?[0-9AC-FHKNPRTV-Y]{4}"""),
        "IS" to digits(3),
        "IT" to digits(5),
        "JP" to pattern("""^\d{3}-?\d{4}$"""),
        "LI" to digits(4),
        "LT" to pattern("""^(LT-\d{5}|\d{4,5})$"""),
        "LU" to digits(4),
        "LV" to pattern("""^(LV-)[0-9]{4}$"""),
        "MC" to pattern("""^980\d{2}$"""),
        "MT" to pattern("""^[A-Za-z]{3}\d{4}$"""),
        "MY" to digits(5),
        "NL" to pattern("""(?:NL-)?(?:[1-9]\d{3} ?(?:[A-EGHJ-NPRTVWXZ][A-EGHJ-NPRSTVWXZ]|S[BCEGHJ-NPRTVWXZ]))"""),
        "NO" to digits(4),
        "PL" to pattern("""^\d{2}[-]{0,1}\d{3}$"""),
        "PT" to pattern("""^([1-9]\d{3})([- ]?(\d{3})? *)$"""),
        "RO" to digits(6),
        "SI" to digits(4),
        "SE" to digits(5),
        "SG" to digits(6),
        "SK" to digits(5),
        "US" to pattern("""^\d{5}(?:-\d{4})?$"""),
    )

    private val MAX_LENGTHS: Map<String, Int> = mapOf(
        "AT" to 4,
        "AU" to 4,
        "BE" to 4,
        "BG" to 4,
        "BR" to 9,
        "CA" to 7,
        "CH" to 4,
        "CY" to 4,
        "CZ" to 6,
        "DE" to 5,
        "DK" to 7,
        "EE" to 5,
        "ES" to 5,
        "FI" to 5,
        "FR" to 5,
        "GB" to 8,
        "GE" to 4,
        "GR" to 6,
        "HR" to 5,
        "HU" to 4,
        "IE" to 8,
        "IS" to 3,
        "IT" to 5,
        "JP" to 8,
        "LI" to 4,
        "LT" to 8,
        "LU" to 4,
        "LV" to 7,
        "MC" to 5,
        "MT" to 8,
        "MY" to 5,
        "NL" to 7,
        "NO" to 4,
        "NZ" to 4,
        "PL" to 6,
        "PT" to 8,
        "RO" to 6,
        "SE" to 5,
        "SG" to 6,
        "SI" to 7,
        "SK" to 8,
        // Web switches between 5 and 10 as a hyphen is typed; a static 10 keeps ZIP+4 typable.
        "US" to POSTAL_CODE_MAX_LENGTH,
    )

    fun getPattern(countryCode: String?): Regex? = countryCode?.let { PATTERNS[it] }

    fun getMaxLength(countryCode: String?): Int = countryCode?.let { MAX_LENGTHS[it] } ?: POSTAL_CODE_MAX_LENGTH

    private fun digits(count: Int) = pattern("""\d{$count}""")

    // Case-insensitive matching is the one deliberate divergence from Web, which is case-sensitive.
    private fun pattern(pattern: String) = Regex(pattern, RegexOption.IGNORE_CASE)
}
