package com.adyen.core.utils;

import android.support.annotation.NonNull;
import android.util.Log;

import com.adyen.core.models.Amount;

import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Currency;
import java.util.Locale;

/**
 * Utility class for parsing/converting Amount strings.
 */

public final class AmountUtil {

    private static final String TAG = AmountUtil.class.getSimpleName();

    private AmountUtil() {
        // default constructor hidden. This class is not supposed to be initialized.
    }

    /**
     * Checks if it is a supported ISO 4217 code by using {@link java.util.Currency#getInstance(String)}.
     * @param currencyCode The currency code to check.
     * @return true if the given currency code is valid, otherwise false.
     */
    public static boolean isValidCurrencyCode(final String currencyCode) {
        if (StringUtils.isEmptyOrNull(currencyCode)) {
            return false;
        }

        // Temporary exception while the Belarus currency change is taking place. It should be removed afterwards
        // but make sure that the Java object supports the new Belarus currency: BYN
        if ("BYR".equals(currencyCode) || "BYN".equals(currencyCode)) {
            return true;
        }
        // End of the temporary exception

        try {
            Currency.getInstance(currencyCode);
        } catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * Parses the amount object and converts it to string.
     *
     * The resulting string will have a currency symbol if it exists. Otherwise, it will include the three character
     * country code. If a symbol exists, there will be no space between currency symbol and amount; e.g.
     * "$100.00". Otherwise; there will be a space in between, e.g. "IDR 100"
     *
     * @param amount {@link Amount} for which the string representation is requested for.
     * @return the string representation of given amount.
     */
    public static String toString(@NonNull final Amount amount) {
        return format(amount, true);
    }

    /**
     * Returns the string representation of the given amount.
     * The currency symbol/code can be included depending on the value of includeCurrencyCode.
     *
     * @param amount {@link Amount} for which the string representation is requested for.
     * @param includeCurrencyCode if true, the currency symbol or code will be included in string.
     * @return the string representation of given amount.
     */
    public static String format(@NonNull final Amount amount, boolean includeCurrencyCode) {
        return format(amount, includeCurrencyCode, null);
    }

    /**
     * Returns the string representation of the given amount.
     * The formatting is done according to selected {@link Locale}.
     *
     * @param amount {@link Amount} for which the string representation is requested for.
     * @param includeCurrencyCode if true, the currency symbol or code will be included in string.
     * @param locale {@link Locale} which will be used for formatting.
     * @return the string representation of given amount.
     */
    public static String format(@NonNull final Amount amount, final boolean includeCurrencyCode, final Locale locale) {
        String valueString;

        long amountValue = amount.getValue();
        final String currencyCode = amount.getCurrency();
        int exponent = getExponent(currencyCode);
        valueString = format(amountValue, exponent, locale);
        if (includeCurrencyCode) {
            final String currencySymbol = getCurrencySymbol(currencyCode);
            final StringBuilder amountStringBuilder = new StringBuilder(currencySymbol);
            // If there is no symbol for the given currency code, there should be a space between currency code
            // and amount
            if (currencySymbol.length() > 1) {
                amountStringBuilder.append(" ");
            }
            amountStringBuilder.append(valueString);
            return amountStringBuilder.toString();
        }
        return valueString;
    }

    public static String format(long amountValue, int exponent, Locale locale) {
        String valueString;
        if (exponent > 0) {
            final String sign = (amountValue < 0) ? "-" : "";
            long absAmountValue = Math.abs(amountValue);
            long power = 1;
            for (int i = 0; i < exponent; i++) {
                power *= 10;
            }
            if (locale != null) {
                DecimalFormatSymbols dfs = DecimalFormatSymbols.getInstance(locale);
                String highValue = NumberFormat.getInstance(locale).format((absAmountValue / power));
                valueString = String.format("%s%s%s%0" + exponent + "d", sign, highValue, dfs.getDecimalSeparator(),
                        (absAmountValue % power));
            } else {
                valueString = String.format("%s%d.%0" + exponent + "d", sign, (absAmountValue / power),
                        (absAmountValue % power));
            }
        } else {
            if (locale != null) {
                valueString = NumberFormat.getInstance(locale).format(amountValue);
            } else {
                valueString = String.valueOf(amountValue);
            }
        }
        return valueString;
    }

    private static String getCurrencySymbol(final String currencyCode) {
        return Currency.getInstance(currencyCode).getSymbol();
    }

    private static int getExponent(@NonNull String currencyCode) {
        // first some overrides:
        if ("ISK".equals(currencyCode)) {
            return 2;
        }
        if ("CLP".equals(currencyCode)) {
            return 2;
        }
        if ("MXP".equals(currencyCode)) {
            return 2;
        }
        if ("MRO".equals(currencyCode)) {
            return 1;
        }
        if ("IDR".equals(currencyCode)) {
            return 0;
        }
        if ("VND".equals(currencyCode)) {
            return 0;
        }
        if ("UGX".equals(currencyCode)) {
            return 0;
        }
        if ("CVE".equals(currencyCode)) {
            return 0;
        }
        if ("ZMW".equals(currencyCode)) {
            return 2;
        }
        if ("GHC".equals(currencyCode)) {
            return 0;
        }
        if ("BYR".equals(currencyCode)) {
            return 0;
        }
        if ("BYN".equals(currencyCode)) {
            return 2;
        }

        // now the default behavior
        Currency curr = null;
        try {
            curr = Currency.getInstance(currencyCode);
        } catch (final IllegalArgumentException exception) {
            Log.d(TAG, "Currency is incorrect: ", exception);
        }
        int exponent = 0;
        if (curr != null) {
            exponent = curr.getDefaultFractionDigits();
            if (exponent == -1) {
                exponent = 0;
            }
        }
        return exponent;
    }

    /**
     * For the given amount string, returns the value in major units taking the currency code into account.
     *
     * @param currencyCode Currency code for the amount.
     * @param amount The amount string which consists of numbers and '.' or ','
     * @return the amount value in major units.
     * @throws ParseException when the given amount string is invalid.
     */
    public static long parseMajorAmount(final String currencyCode, final String amount) throws ParseException {
        //TODO: This method needs refactoring; it looks unnecessarily complex.
        final int exponent = getExponent(currencyCode);
        String amt = amount;
        if (amt.indexOf(',') < amt.indexOf('.')) {
            // remove the commas
            amt = amt.replace(",", "");
        }

        if (amt.indexOf('.') < amt.indexOf(',')) {
            // remove the dots
            amt = amt.replace(".", "");
        }

        char[] ch = amt.trim().toCharArray();
        int idx = 0;

        boolean negative = false;
        long major = 0;
        boolean hasMinor = false;
        int minorDigits = 0;
        long minor = 0;

        if (ch.length == 0) {
            throw new ParseException("Empty string is not an amount", 0);
        }
        if (ch[idx] == '-') {
            negative = true;
            idx++;
        }
        while (idx < ch.length) {
            if (ch[idx] >= '0' && ch[idx] <= '9') {
                major *= 10;
                major += (ch[idx] - '0');
                idx++;
            } else if (Character.isWhitespace(ch[idx])) {
                idx++;
            } else {
                break;
            }
        }
        if (idx < ch.length && (ch[idx] == '.' || ch[idx] == ',')) {
            idx++;
            hasMinor = true;
            while (idx < ch.length) {
                if (ch[idx] >= '0' && ch[idx] <= '9') {
                    minor *= 10;
                    minor += (ch[idx] - '0');
                    idx++;
                    minorDigits++;
                } else if (Character.isWhitespace(ch[idx])) {
                    idx++;
                } else {
                    break;
                }
            }
        }

        // Sometimes negative gets put after the number
        if (idx < ch.length && ch[idx] == '-') {
            negative = true;
            idx++;
        }

        if (hasMinor && minorDigits != exponent && minor != 0 && !(minorDigits > exponent)) {
            throw new ParseException("Number of minor currency digits in amount (" + minorDigits + ") does not match"
                    + " currency definition (" + exponent + "). Amount was " + amount, 0);
        }
        long power = 1;
        for (int i = 0; i < exponent; i++) {
            power *= 10;
        }
        return (minor + (power * major)) * (negative ? -1 : 1);
    }

}
