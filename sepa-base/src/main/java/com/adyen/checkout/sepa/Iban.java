/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 13/08/2017.
 */

package com.adyen.checkout.sepa;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.code.Lint;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("MagicNumber")
public final class Iban {

    @SuppressWarnings(Lint.SYNTHETIC)
    static final int IBAN_BLOCK_SIZE = 4;
    private static final int COUNTRY_NAME_SIZE = 2;
    private static final int COUNTRY_CODE_POSITION_END = 2;
    private static final int CHECK_DIGIT_POSITION_START = 2;
    private static final int CHECK_DIGIT_POSITION_END = 4;

    /**
     * Based on https://www.ecb.europa.eu (SEPA Countries) and https://en.wikipedia.org (Single Euro Payments Area).
     */
    private static final Map<String, Details> COUNTRY_DETAILS;

    static {
        final HashMap<String, Details> hashMap = new HashMap<>();

        hashMap.put("AD", new Details(Pattern.compile("^AD\\d{10}[0-9A-Z]{12}$"), 24));
        hashMap.put("AE", new Details(Pattern.compile("^AE\\d{21}$"), 23));
        hashMap.put("AL", new Details(Pattern.compile("^AL\\d{10}[0-9A-Z]{16}$"), 28));
        hashMap.put("AT", new Details(Pattern.compile("^AT\\d{18}$"), 20, true));
        hashMap.put("BA", new Details(Pattern.compile("^BA\\d{18}$"), 20));
        hashMap.put("BE", new Details(Pattern.compile("^BE\\d{14}$"), 16, true));
        hashMap.put("BG", new Details(Pattern.compile("^BG\\d{2}[A-Z]{4}\\d{6}[0-9A-Z]{8}$"), 22, true));
        hashMap.put("BH", new Details(Pattern.compile("^BH\\d{2}[A-Z]{4}[0-9A-Z]{14}$"), 22));
        hashMap.put("CH", new Details(Pattern.compile("^CH\\d{7}[0-9A-Z]{12}$"), 21, true));
        hashMap.put("CY", new Details(Pattern.compile("^CY\\d{10}[0-9A-Z]{16}$"), 21, true));
        hashMap.put("CZ", new Details(Pattern.compile("^CZ\\d{22}$"), 24, true));
        hashMap.put("DE", new Details(Pattern.compile("^DE\\d{20}$"), 22, true));
        hashMap.put("DK", new Details(Pattern.compile("^DK\\d{16}$|^FO\\d{16}$|^GL\\d{16}$"), 18, true));
        hashMap.put("DO", new Details(Pattern.compile("^DO\\d{2}[0-9A-Z]{4}\\d{20}$"), 28));
        hashMap.put("EE", new Details(Pattern.compile("^EE\\d{18}$"), 20, true));
        hashMap.put("ES", new Details(Pattern.compile("^ES\\d{22}$"), 24, true));
        hashMap.put("FI", new Details(Pattern.compile("^FI\\d{16}$"), 18, true));
        hashMap.put("FR", new Details(Pattern.compile("^FR\\d{12}[0-9A-Z]{11}\\d{2}$"), 27, true));
        hashMap.put("GB", new Details(Pattern.compile("^GB\\d{2}[A-Z]{4}\\d{14}$"), 22, true));
        hashMap.put("GE", new Details(Pattern.compile("^GE\\d{2}[A-Z]{2}\\d{16}$"), 22));
        hashMap.put("GI", new Details(Pattern.compile("^GI\\d{2}[A-Z]{4}[0-9A-Z]{15}$"), 23));
        hashMap.put("GR", new Details(Pattern.compile("^GR\\d{9}[0-9A-Z]{16}$"), 27, true));
        hashMap.put("HR", new Details(Pattern.compile("^HR\\d{19}$"), 21, true));
        hashMap.put("HU", new Details(Pattern.compile("^HU\\d{26}$"), 28, true));
        hashMap.put("IE", new Details(Pattern.compile("^IE\\d{2}[A-Z]{4}\\d{14}$"), 22, true));
        hashMap.put("IL", new Details(Pattern.compile("^IL\\d{21}$"), 23));
        hashMap.put("IS", new Details(Pattern.compile("^IS\\d{24}$"), 26, true));
        hashMap.put("IT", new Details(Pattern.compile("^IT\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}$"), 27, true));
        hashMap.put("KW", new Details(Pattern.compile("^KW\\d{2}[A-Z]{4}22!$"), 30));
        hashMap.put("KZ", new Details(Pattern.compile("^[A-Z]{2}\\d{5}[0-9A-Z]{13}$"), 20));
        hashMap.put("LB", new Details(Pattern.compile("^LB\\d{6}[0-9A-Z]{20}$"), 28));
        hashMap.put("LI", new Details(Pattern.compile("^LI\\d{7}[0-9A-Z]{12}$"), 21, true));
        hashMap.put("LT", new Details(Pattern.compile("^LT\\d{18}$"), 20, true));
        hashMap.put("LU", new Details(Pattern.compile("^LU\\d{5}[0-9A-Z]{13}$"), 20, true));
        hashMap.put("LV", new Details(Pattern.compile("^LV\\d{2}[A-Z]{4}[0-9A-Z]{13}$"), 21, true));
        hashMap.put("MC", new Details(Pattern.compile("^MC\\d{12}[0-9A-Z]{11}\\d{2}$"), 27, true));
        hashMap.put("ME", new Details(Pattern.compile("^ME\\d{20}$"), 22));
        hashMap.put("MK", new Details(Pattern.compile("^MK\\d{5}[0-9A-Z]{10}\\d{2}$"), 19));
        hashMap.put("MR", new Details(Pattern.compile("^MR13\\d{23}$"), 27));
        hashMap.put("MT", new Details(Pattern.compile("^MT\\d{2}[A-Z]{4}\\d{5}[0-9A-Z]{18}$"), 31, true));
        hashMap.put("MU", new Details(Pattern.compile("^MU\\d{2}[A-Z]{4}\\d{19}[A-Z]{3}$"), 30));
        hashMap.put("NL", new Details(Pattern.compile("^NL\\d{2}[A-Z]{4}\\d{10}$"), 18, true));
        hashMap.put("NO", new Details(Pattern.compile("^NO\\d{13}$"), 15, true));
        hashMap.put("PL", new Details(Pattern.compile("^PL\\d{10}[0-9A-Z]{16}$"), 28, true));
        hashMap.put("PT", new Details(Pattern.compile("^PT\\d{23}$"), 25, true));
        hashMap.put("RO", new Details(Pattern.compile("^RO\\d{2}[A-Z]{4}[0-9A-Z]{16}$"), 24, true));
        hashMap.put("RS", new Details(Pattern.compile("^RS\\d{20}$"), 22));
        hashMap.put("SA", new Details(Pattern.compile("^SA\\d{4}[0-9A-Z]{18}$"), 24));
        hashMap.put("SE", new Details(Pattern.compile("^SE\\d{22}$"), 24, true));
        hashMap.put("SI", new Details(Pattern.compile("^SI\\d{17}$"), 19, true));
        hashMap.put("SK", new Details(Pattern.compile("^SK\\d{22}$"), 24, true));
        hashMap.put("SM", new Details(Pattern.compile("^SM\\d{2}[A-Z]\\d{10}[0-9A-Z]{12}$"), 27, true));
        hashMap.put("TN", new Details(Pattern.compile("^TN59\\d{20}$"), 24));
        hashMap.put("TR", new Details(Pattern.compile("^TR\\d{7}[0-9A-Z]{17}$"), 26));

        COUNTRY_DETAILS = Collections.unmodifiableMap(hashMap);
    }

    private static final BigInteger VALIDATION_MODULUS = new BigInteger("97");

    private final String mValue;

    /**
     * Formats an IBAN value with spaces.
     *
     * @param ibanValue The IBAN value to format.
     * @return The formatted IBAN value.
     */
    @NonNull
    public static String format(@Nullable String ibanValue) {
        final String normalizedValue = normalize(ibanValue);
        return normalizedValue.replaceAll("(.{4})", "$1 ").trim();
    }

    /**
     * Masks an IBAN value for displaying it in the user interface.
     *
     * @param ibanValue The IBAN value to mask.
     * @return The masked IBAN value.
     */
    @NonNull
    @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
    public static String mask(@Nullable String ibanValue) {
        final String normalizedValue = normalize(ibanValue);
        return normalizedValue.replaceFirst("(.{4}).+(.{4})", "$1 \u2026 $2");
    }

    /**
     * Parses an {@link Iban}.
     *
     * @param value The value to be parsed.
     * @return An {@link Iban} if the given value is valid, otherwise {@code null}.
     */
    @Nullable
    public static Iban parse(@Nullable String value) {
        final String normalizedValue = normalize(value);
        final Details details = normalizedValue.length() >= 2 ? COUNTRY_DETAILS.get(normalizedValue.substring(0, 2)) : null;

        if (details != null && details.isFullMatch(normalizedValue) && isChecksumValid(normalizedValue)) {
            return new Iban(normalizedValue);

        }

        return null;
    }

    /**
     * Parses an {@link Iban} by adding missing zeros after the last block of letters, e.g. NL13 TEST 1234 5678 9 becomes NL13 TEST 0123 4567 89.
     *
     * @param value The value to be parsed.
     * @return An {@link Iban} if the given can be parsed by adding zeros, otherwise {@code null}.
     */
    @Nullable
    public static Iban parseByAddingMissingZeros(@Nullable String value) {
        final String normalizedValue = normalize(value);
        final Details details = normalizedValue.length() >= 2 ? COUNTRY_DETAILS.get(normalizedValue.substring(0, 2)) : null;

        if (details != null) {
            final String zeroPadded = getZeroPaddedValue(normalizedValue, details);
            if (details.isFullMatch(zeroPadded) && isChecksumValid(zeroPadded)) {
                return new Iban(zeroPadded);
            }
        }

        return null;
    }

    /**
     * Checks whether a given value is a partial {@link Iban}, i.e. whether it is a prefix of a valid IBAN.
     *
     * @param value The value to check.
     * @return {@code true} if the value is a partial IBAN.
     */
    public static boolean isPartial(@Nullable String value) {
        final String normalizedValue = normalize(value);

        if (normalizedValue.length() < COUNTRY_NAME_SIZE) {
            for (String countryCode : COUNTRY_DETAILS.keySet()) {
                if (countryCode.startsWith(normalizedValue)) {
                    return true;
                }
            }
            return false;
        } else {
            final Details details = COUNTRY_DETAILS.get(normalizedValue.substring(0, 2));
            return details != null && details.isPotentialMatchWithMoreInput(normalizedValue);
        }
    }

    /**
     * Checks whether a given value starts with a SEPA country code.
     *
     * @param value The value to check.
     * @return Whether the value starts with a SEPA country code.
     */
    public static boolean startsWithSepaCountryCode(@Nullable String value) {
        final String normalizedValue = normalize(value);

        if (normalizedValue.length() < COUNTRY_NAME_SIZE) {
            for (Map.Entry<String, Details> entry : COUNTRY_DETAILS.entrySet()) {
                final String countryCode = entry.getKey();
                final Details details = entry.getValue();

                if (countryCode.startsWith(normalizedValue) && details != null && details.isSepa()) {
                    return true;
                }
            }
            return false;
        } else {
            final Details details = COUNTRY_DETAILS.get(normalizedValue.substring(0, 2));

            return details != null && details.isSepa();
        }
    }

    /**
     * @return the maximum possible length of an Iban string after being formatted with spaces.
     */
    public static int getFormattedMaxLength() {
        int maxLength = 0;
        for (Details details : COUNTRY_DETAILS.values()) {
            if (details.getLength() > maxLength) {
                maxLength = details.getLength();
            }
        }
        final int spaces = maxLength / IBAN_BLOCK_SIZE - 1;
        return maxLength + spaces;
    }

    @NonNull
    private static String normalize(@Nullable String value) {
        return value != null ? value.replaceAll("[^\\a-zA-Z]&&[^\\d]", "").replaceAll("\\s", "").toUpperCase(Locale.ROOT) : "";
    }

    private static boolean isChecksumValid(@NonNull String normalizedIban) {
        final String rearrangedIban = normalizedIban.substring(IBAN_BLOCK_SIZE) + normalizedIban.substring(0, IBAN_BLOCK_SIZE);

        final StringBuilder numericIban = new StringBuilder();

        for (int i = 0; i < rearrangedIban.length(); i++) {
            numericIban.append(Character.getNumericValue(rearrangedIban.charAt(i)));
        }

        final BigInteger numericIbanValue = new BigInteger(numericIban.toString());

        return numericIbanValue.mod(VALIDATION_MODULUS).intValue() == 1;
    }

    @NonNull
    @SuppressWarnings("checkstyle:MagicNumber")
    private static String getZeroPaddedValue(@NonNull String normalizedValue, @NonNull Details details) {
        final int length = normalizedValue.length();
        final int difference = details.getLength() - length;

        if (difference > 0 && difference <= 3) {
            int lastDigitIndex = -1;

            // Check from index after check digits
            for (int i = length - 1; i > 4; i--) {
                if (Character.isDigit(normalizedValue.charAt(i))) {
                    lastDigitIndex = i;
                } else {
                    break;
                }
            }

            if (lastDigitIndex > 0) {
                final char[] chars = new char[details.getLength() - length];
                Arrays.fill(chars, '0');
                return normalizedValue.substring(0, lastDigitIndex) + new String(chars) + normalizedValue.substring(lastDigitIndex, length);
            }
        }

        return normalizedValue;
    }

    private Iban(@NonNull String value) {
        mValue = value;
    }

    @NonNull
    public String getValue() {
        return mValue;
    }

    @NonNull
    public String getCountryCode() {
        return mValue.substring(0, COUNTRY_CODE_POSITION_END);
    }

    @NonNull
    public String getCheckDigits() {
        return mValue.substring(CHECK_DIGIT_POSITION_START, CHECK_DIGIT_POSITION_END);
    }

    @NonNull
    public String getBban() {
        return mValue.substring(IBAN_BLOCK_SIZE);
    }

    /**
     * Check if the this IBAN is from a SEPA country.
     *
     * @return If is from a SEPA country.
     */
    public boolean isSepa() {
        final Details details = COUNTRY_DETAILS.get(getCountryCode());
        return details != null && details.isSepa();
    }

    private static final class Details {
        private final Pattern mPattern;

        private final int mLength;

        private final boolean mSepa;

        Details(@NonNull Pattern pattern, int length) {
            this(pattern, length, false);
        }

        Details(@NonNull Pattern pattern, int length, boolean sepa) {
            mPattern = pattern;
            mLength = length;
            mSepa = sepa;
        }

        @NonNull
        private Pattern getPattern() {
            return mPattern;
        }

        int getLength() {
            return mLength;
        }

        boolean isSepa() {
            return mSepa;
        }

        boolean isFullMatch(@NonNull String normalizedIban) {
            return mLength == normalizedIban.length() && mPattern.matcher(normalizedIban).matches();
        }

        boolean isPotentialMatchWithMoreInput(@NonNull String normalizedIban) {
            if (mLength > normalizedIban.length()) {
                final Matcher matcher = mPattern.matcher(normalizedIban);
                // noinspection ResultOfMethodCallIgnored, needs to be called, but is not relevant.
                matcher.matches();

                return matcher.hitEnd();
            } else {
                return false;
            }
        }
    }
}
