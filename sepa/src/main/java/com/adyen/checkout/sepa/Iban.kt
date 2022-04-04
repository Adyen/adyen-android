/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 13/08/2017.
 */
package com.adyen.checkout.sepa

import java.math.BigInteger
import java.util.Arrays
import java.util.Collections
import java.util.Locale
import java.util.regex.Pattern

class Iban private constructor(val value: String) {

    @Suppress("MemberVisibilityCanBePrivate")
    val countryCode: String = value.substring(0, COUNTRY_CODE_POSITION_END)
    val checkDigits: String = value.substring(CHECK_DIGIT_POSITION_START, CHECK_DIGIT_POSITION_END)

    @Suppress("unused")
    val bban: String = value.substring(IBAN_BLOCK_SIZE)

    /**
     * Check if the this IBAN is from a SEPA country.
     *
     * @return If is from a SEPA country.
     */
    @Suppress("unused")
    val isSepa: Boolean
        get() {
            val details = COUNTRY_DETAILS[countryCode]
            return details != null && details.isSepa
        }

    private class Details @JvmOverloads constructor(
        private val pattern: Pattern,
        val length: Int,
        val isSepa: Boolean = false,
    ) {
        fun isFullMatch(normalizedIban: String): Boolean {
            return length == normalizedIban.length && pattern.matcher(normalizedIban).matches()
        }

        fun isPotentialMatchWithMoreInput(normalizedIban: String): Boolean {
            return if (length > normalizedIban.length) {
                val matcher = pattern.matcher(normalizedIban)
                // noinspection ResultOfMethodCallIgnored, needs to be called, but is not relevant.
                matcher.matches()
                matcher.hitEnd()
            } else {
                false
            }
        }
    }

    @Suppress("MagicNumber")
    companion object {
        const val IBAN_BLOCK_SIZE = 4
        private const val COUNTRY_NAME_SIZE = 2
        private const val COUNTRY_CODE_POSITION_END = 2
        private const val CHECK_DIGIT_POSITION_START = 2
        private const val CHECK_DIGIT_POSITION_END = 4

        /**
         * Based on https://www.ecb.europa.eu (SEPA Countries) and https://en.wikipedia.org (Single Euro Payments Area).
         */
        private val COUNTRY_DETAILS: Map<String, Details>
        private val VALIDATION_MODULUS = BigInteger("97")

        init {
            val hashMap = HashMap<String, Details>()
            hashMap["AD"] = Details(Pattern.compile("^AD\\d{10}[0-9A-Z]{12}$"), 24)
            hashMap["AE"] = Details(Pattern.compile("^AE\\d{21}$"), 23)
            hashMap["AL"] = Details(Pattern.compile("^AL\\d{10}[0-9A-Z]{16}$"), 28)
            hashMap["AT"] = Details(Pattern.compile("^AT\\d{18}$"), 20, true)
            hashMap["BA"] = Details(Pattern.compile("^BA\\d{18}$"), 20)
            hashMap["BE"] = Details(Pattern.compile("^BE\\d{14}$"), 16, true)
            hashMap["BG"] = Details(Pattern.compile("^BG\\d{2}[A-Z]{4}\\d{6}[0-9A-Z]{8}$"), 22, true)
            hashMap["BH"] = Details(Pattern.compile("^BH\\d{2}[A-Z]{4}[0-9A-Z]{14}$"), 22)
            hashMap["CH"] = Details(Pattern.compile("^CH\\d{7}[0-9A-Z]{12}$"), 21, true)
            hashMap["CY"] = Details(Pattern.compile("^CY\\d{10}[0-9A-Z]{16}$"), 21, true)
            hashMap["CZ"] = Details(Pattern.compile("^CZ\\d{22}$"), 24, true)
            hashMap["DE"] = Details(Pattern.compile("^DE\\d{20}$"), 22, true)
            hashMap["DK"] = Details(Pattern.compile("^DK\\d{16}$|^FO\\d{16}$|^GL\\d{16}$"), 18, true)
            hashMap["DO"] = Details(Pattern.compile("^DO\\d{2}[0-9A-Z]{4}\\d{20}$"), 28)
            hashMap["EE"] = Details(Pattern.compile("^EE\\d{18}$"), 20, true)
            hashMap["ES"] = Details(Pattern.compile("^ES\\d{22}$"), 24, true)
            hashMap["FI"] = Details(Pattern.compile("^FI\\d{16}$"), 18, true)
            hashMap["FR"] = Details(Pattern.compile("^FR\\d{12}[0-9A-Z]{11}\\d{2}$"), 27, true)
            hashMap["GB"] = Details(Pattern.compile("^GB\\d{2}[A-Z]{4}\\d{14}$"), 22, true)
            hashMap["GE"] = Details(Pattern.compile("^GE\\d{2}[A-Z]{2}\\d{16}$"), 22)
            hashMap["GI"] = Details(Pattern.compile("^GI\\d{2}[A-Z]{4}[0-9A-Z]{15}$"), 23)
            hashMap["GR"] = Details(Pattern.compile("^GR\\d{9}[0-9A-Z]{16}$"), 27, true)
            hashMap["HR"] = Details(Pattern.compile("^HR\\d{19}$"), 21, true)
            hashMap["HU"] = Details(Pattern.compile("^HU\\d{26}$"), 28, true)
            hashMap["IE"] = Details(Pattern.compile("^IE\\d{2}[A-Z]{4}\\d{14}$"), 22, true)
            hashMap["IL"] = Details(Pattern.compile("^IL\\d{21}$"), 23)
            hashMap["IS"] = Details(Pattern.compile("^IS\\d{24}$"), 26, true)
            hashMap["IT"] = Details(Pattern.compile("^IT\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}$"), 27, true)
            hashMap["KW"] = Details(Pattern.compile("^KW\\d{2}[A-Z]{4}22!$"), 30)
            hashMap["KZ"] = Details(Pattern.compile("^[A-Z]{2}\\d{5}[0-9A-Z]{13}$"), 20)
            hashMap["LB"] = Details(Pattern.compile("^LB\\d{6}[0-9A-Z]{20}$"), 28)
            hashMap["LI"] = Details(Pattern.compile("^LI\\d{7}[0-9A-Z]{12}$"), 21, true)
            hashMap["LT"] = Details(Pattern.compile("^LT\\d{18}$"), 20, true)
            hashMap["LU"] = Details(Pattern.compile("^LU\\d{5}[0-9A-Z]{13}$"), 20, true)
            hashMap["LV"] = Details(Pattern.compile("^LV\\d{2}[A-Z]{4}[0-9A-Z]{13}$"), 21, true)
            hashMap["MC"] = Details(Pattern.compile("^MC\\d{12}[0-9A-Z]{11}\\d{2}$"), 27, true)
            hashMap["ME"] = Details(Pattern.compile("^ME\\d{20}$"), 22)
            hashMap["MK"] = Details(Pattern.compile("^MK\\d{5}[0-9A-Z]{10}\\d{2}$"), 19)
            hashMap["MR"] = Details(Pattern.compile("^MR13\\d{23}$"), 27)
            hashMap["MT"] = Details(Pattern.compile("^MT\\d{2}[A-Z]{4}\\d{5}[0-9A-Z]{18}$"), 31, true)
            hashMap["MU"] = Details(Pattern.compile("^MU\\d{2}[A-Z]{4}\\d{19}[A-Z]{3}$"), 30)
            hashMap["NL"] = Details(Pattern.compile("^NL\\d{2}[A-Z]{4}\\d{10}$"), 18, true)
            hashMap["NO"] = Details(Pattern.compile("^NO\\d{13}$"), 15, true)
            hashMap["PL"] = Details(Pattern.compile("^PL\\d{10}[0-9A-Z]{16}$"), 28, true)
            hashMap["PT"] = Details(Pattern.compile("^PT\\d{23}$"), 25, true)
            hashMap["RO"] = Details(Pattern.compile("^RO\\d{2}[A-Z]{4}[0-9A-Z]{16}$"), 24, true)
            hashMap["RS"] = Details(Pattern.compile("^RS\\d{20}$"), 22)
            hashMap["SA"] = Details(Pattern.compile("^SA\\d{4}[0-9A-Z]{18}$"), 24)
            hashMap["SE"] = Details(Pattern.compile("^SE\\d{22}$"), 24, true)
            hashMap["SI"] = Details(Pattern.compile("^SI\\d{17}$"), 19, true)
            hashMap["SK"] = Details(Pattern.compile("^SK\\d{22}$"), 24, true)
            hashMap["SM"] = Details(Pattern.compile("^SM\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}$"), 27, true)
            hashMap["TN"] = Details(Pattern.compile("^TN59\\d{20}$"), 24)
            hashMap["TR"] = Details(Pattern.compile("^TR\\d{7}[0-9A-Z]{17}$"), 26)
            COUNTRY_DETAILS = Collections.unmodifiableMap(hashMap)
        }

        /**
         * Formats an IBAN value with spaces.
         *
         * @param ibanValue The IBAN value to format.
         * @return The formatted IBAN value.
         */
        @JvmStatic
        fun format(ibanValue: String?): String {
            val normalizedValue = normalize(ibanValue)
            return normalizedValue.replace("(.{4})".toRegex(), "$1 ").trim { it <= ' ' }
        }

        /**
         * Masks an IBAN value for displaying it in the user interface.
         *
         * @param ibanValue The IBAN value to mask.
         * @return The masked IBAN value.
         */
        @Suppress("unused")
        fun mask(ibanValue: String?): String {
            val normalizedValue = normalize(ibanValue)
            return normalizedValue.replaceFirst("(.{4}).+(.{4})".toRegex(), "$1 \u2026 $2")
        }

        /**
         * Parses an [Iban].
         *
         * @param value The value to be parsed.
         * @return An [Iban] if the given value is valid, otherwise `null`.
         */
        @JvmStatic
        fun parse(value: String?): Iban? {
            val normalizedValue = normalize(value)
            val details = if (normalizedValue.length >= 2) COUNTRY_DETAILS[normalizedValue.substring(0, 2)] else null
            return if (details != null && details.isFullMatch(normalizedValue) && isChecksumValid(normalizedValue)) {
                Iban(normalizedValue)
            } else null
        }

        /**
         * Parses an [Iban] by adding missing zeros after the last block of letters, e.g. NL13 TEST 1234 5678 9 becomes NL13 TEST 0123 4567 89.
         *
         * @param value The value to be parsed.
         * @return An [Iban] if the given can be parsed by adding zeros, otherwise `null`.
         */
        @Suppress("unused")
        fun parseByAddingMissingZeros(value: String?): Iban? {
            val normalizedValue = normalize(value)
            val details = if (normalizedValue.length >= 2) COUNTRY_DETAILS[normalizedValue.substring(0, 2)] else null
            if (details != null) {
                val zeroPadded = getZeroPaddedValue(normalizedValue, details)
                if (details.isFullMatch(zeroPadded) && isChecksumValid(zeroPadded)) {
                    return Iban(zeroPadded)
                }
            }
            return null
        }

        /**
         * Checks whether a given value is a partial [Iban], i.e. whether it is a prefix of a valid IBAN.
         *
         * @param value The value to check.
         * @return `true` if the value is a partial IBAN.
         */
        @Suppress("unused")
        fun isPartial(value: String?): Boolean {
            val normalizedValue = normalize(value)
            return if (normalizedValue.length < COUNTRY_NAME_SIZE) {
                for (countryCode in COUNTRY_DETAILS.keys) {
                    if (countryCode.startsWith(normalizedValue)) {
                        return true
                    }
                }
                false
            } else {
                val details = COUNTRY_DETAILS[normalizedValue.substring(0, 2)]
                details != null && details.isPotentialMatchWithMoreInput(normalizedValue)
            }
        }

        /**
         * Checks whether a given value starts with a SEPA country code.
         *
         * @param value The value to check.
         * @return Whether the value starts with a SEPA country code.
         */
        @Suppress("unused")
        fun startsWithSepaCountryCode(value: String?): Boolean {
            val normalizedValue = normalize(value)
            return if (normalizedValue.length < COUNTRY_NAME_SIZE) {
                for ((countryCode, details) in COUNTRY_DETAILS) {
                    if (countryCode.startsWith(normalizedValue) && details.isSepa) {
                        return true
                    }
                }
                false
            } else {
                val details = COUNTRY_DETAILS[normalizedValue.substring(0, 2)]
                details != null && details.isSepa
            }
        }

        /**
         * @return the maximum possible length of an Iban string after being formatted with spaces.
         */
        @JvmStatic
        val formattedMaxLength: Int
            get() {
                var maxLength = 0
                for (details in COUNTRY_DETAILS.values) {
                    if (details.length > maxLength) {
                        maxLength = details.length
                    }
                }
                val spaces = maxLength / IBAN_BLOCK_SIZE - 1
                return maxLength + spaces
            }

        private fun normalize(value: String?): String {
            return value
                ?.replace("[^\\a-zA-Z]&&[^\\d]".toRegex(), "")
                ?.replace("\\s".toRegex(), "")
                ?.uppercase(Locale.ROOT) ?: ""
        }

        private fun isChecksumValid(normalizedIban: String): Boolean {
            val rearrangedIban =
                normalizedIban.substring(IBAN_BLOCK_SIZE) + normalizedIban.substring(0, IBAN_BLOCK_SIZE)
            val numericIban = StringBuilder()
            for (element in rearrangedIban) {
                numericIban.append(Character.getNumericValue(element))
            }
            val numericIbanValue = BigInteger(numericIban.toString())
            return numericIbanValue.mod(VALIDATION_MODULUS).toInt() == 1
        }

        @Suppress("MagicNumber")
        private fun getZeroPaddedValue(normalizedValue: String, details: Details): String {
            val length = normalizedValue.length
            val difference = details.length - length
            if (difference in 1..3) {
                var lastDigitIndex = -1

                // Check from index after check digits
                for (i in length - 1 downTo 5) {
                    lastDigitIndex = if (Character.isDigit(normalizedValue[i])) {
                        i
                    } else {
                        break
                    }
                }
                if (lastDigitIndex > 0) {
                    val chars = CharArray(details.length - length)
                    Arrays.fill(chars, '0')
                    return normalizedValue.substring(0, lastDigitIndex) + String(chars) + normalizedValue.substring(
                        lastDigitIndex,
                        length
                    )
                }
            }
            return normalizedValue
        }
    }
}
