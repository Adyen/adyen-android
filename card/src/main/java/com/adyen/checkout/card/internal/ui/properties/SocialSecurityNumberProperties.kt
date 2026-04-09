/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/3/2026.
 */

package com.adyen.checkout.card.internal.ui.properties

import com.adyen.checkout.ui.internal.element.input.TextFieldSeparator

/**
 * This field has two valid formats:
 * - CPF: 11 digits, formatted as such 123.123.123-12
 * - CNPJ: 14 digits, formatted as such 12.123.123/1234-12
 *
 * The formatting follows the CPF format as long as the length is less than 11 digits. Then between 12 and 14 digits,
 * the CNPJ format is used.
 */
internal object SocialSecurityNumberProperties {

    const val CPF_VALID_LENGTH = 11
    const val CNPJ_VALID_LENGTH = 14

    // same as the CNPJ (the longer format)
    const val SOCIAL_SECURITY_MAX_LENGTH = 14

    // e.g 123.123.123-12
    val CPF_SEPARATORS = listOf(
        TextFieldSeparator('.', 3), // . at index 3
        TextFieldSeparator('.', 6), // . at index 6
        TextFieldSeparator('-', 9), // - at index 9
    )

    // e.g 12.123.123/1234-12
    val CNPJ_SEPARATORS = listOf(
        TextFieldSeparator('.', 2), // . at index 2
        TextFieldSeparator('.', 5), // . at index 5
        TextFieldSeparator('/', 8), // . at index 8
        TextFieldSeparator('-', 12), // - at index 12
    )

    val SOCIAL_SECURITY_SEPARATORS: List<Char> = listOf('.', '-', '/')
}
