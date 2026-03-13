/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.view

internal object SocialSecurityNumberProperties {

    const val CPF_LENGTH_LIMIT = 11

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

    const val MAX_LENGTH_UNFORMATTED = 14
}

internal data class Separator(
    val character: Char,
    val index: Int,
)
