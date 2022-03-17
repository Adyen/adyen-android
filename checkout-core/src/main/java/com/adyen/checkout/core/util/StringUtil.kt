/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */
package com.adyen.checkout.core.util

object StringUtil {
    /**
     * Removes empty spaces and any additional specified characters.
     *
     * @param value The string to be normalized.
     * @param additionalCharsToReplace Additional characters to be removed.
     * @return The original string normalized to remove specified characters.
     */
    @JvmStatic
    fun normalize(value: String, vararg additionalCharsToReplace: Char): String {
        val regex: Regex = "[\\s${String(additionalCharsToReplace)}]".toRegex()
        return value.replace(regex, "")
    }

    /**
     * Check if the string only contains number and the specified separator characters.
     *
     * @param value The string to be checked.
     * @param separators The optional accepted separators.
     * @return If the string is only numbers and separators.
     */
    fun isDigitsAndSeparatorsOnly(value: String, vararg separators: Char): Boolean {
        for (char in value) {
            val isDigitOrSeparator = Character.isDigit(char) || (separators.isNotEmpty() && separators.contains(char))
            if (!isDigitOrSeparator) {
                return false
            }
        }
        return true
    }
}
