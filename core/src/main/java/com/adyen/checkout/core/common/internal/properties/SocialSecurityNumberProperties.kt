/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.core.common.internal.properties

import androidx.annotation.RestrictTo

/**
 * This field has two valid formats:
 * - CPF: 11 digits, formatted as such 123.123.123-12
 * - CNPJ: 14 digits, formatted as such 12.123.123/1234-12
 *
 * The formatting follows the CPF format as long as the length is less than 11 digits. Then between 12 and 14 digits,
 * the CNPJ format is used.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object SocialSecurityNumberProperties {

    const val CPF_VALID_LENGTH = 11
    const val CNPJ_VALID_LENGTH = 14

    // same as the CNPJ (the longer format)
    const val MAX_LENGTH_UNFORMATTED = 14

    // e.g 123.123.123-12
    val CPF_SEPARATORS = listOf(
        Separator('.', 3), // . at index 3
        Separator('.', 7), // . at index 7 (index should include previous separators)
        Separator('-', 11), // - at index 11
    )

    // e.g 12.123.123/1234-12
    val CNPJ_SEPARATORS = listOf(
        Separator('.', 2), // . at index 2
        Separator('.', 6), // . at index 6
        Separator('/', 10), // . at index 10
        Separator('-', 15), // - at index 15
    )

    val ALL_SEPARATORS: List<Char> = (CPF_SEPARATORS + CNPJ_SEPARATORS).map { it.character }
}

// TODO move this to a common module
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class Separator(
    val character: Char,
    val index: Int,
)
