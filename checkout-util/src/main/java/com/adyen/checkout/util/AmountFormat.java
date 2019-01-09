/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 11/08/2017.
 */

package com.adyen.checkout.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.SuperscriptSpan;
import android.util.Log;

import com.adyen.checkout.core.model.Amount;
import com.adyen.checkout.util.internal.CheckoutCurrency;
import com.adyen.checkout.util.internal.TextFormat;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Currency;
import java.util.Locale;

public final class AmountFormat {
    private static final String TAG = AmountFormat.class.getSimpleName();

    /**
     * Format an {@link Amount} to a human readable format.
     *
     * @param context Any app {@link Context}.
     * @param amount An {@link Amount}.
     * @return The formatted amount.
     */
    @NonNull
    public static CharSequence format(@NonNull Context context, @NonNull Amount amount) {
        return format(context, amount.getValue(), amount.getCurrency());
    }

    /**
     * Format an {@link Amount} to a human readable format.
     *
     * @param context Any app {@link Context}.
     * @param value The amount value.
     * @param currencyCode The amount currency code.
     * @return The formatted amount.
     */
    @NonNull
    public static CharSequence format(@NonNull Context context, long value, @NonNull String currencyCode) {
        Locale locale = LocaleUtil.getLocale(context);

        BigDecimal decimalValue = toBigDecimal(value, currencyCode);
        int fractionDigits = decimalValue.scale();

        CharSequence formattedAmount;
        try {
            Currency currency = Currency.getInstance(currencyCode);
            NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance(locale);
            currencyFormat.setCurrency(currency);
            currencyFormat.setMinimumFractionDigits(fractionDigits);
            currencyFormat.setMaximumFractionDigits(fractionDigits);
            formattedAmount = currencyFormat.format(decimalValue);
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Could not determine currency for code." + currencyCode, e);
            //fallback to generic formatting
            NumberFormat numberFormat = DecimalFormat.getInstance(locale);
            numberFormat.setMinimumFractionDigits(fractionDigits);
            numberFormat.setMaximumFractionDigits(fractionDigits);
            String formattedValue = numberFormat.format(decimalValue);
            formattedAmount = new SpannableStringBuilder(context.getString(R.string.amount_format, currencyCode, formattedValue));
        }

        SpannableStringBuilder builder = new SpannableStringBuilder(formattedAmount);
        String[] parts = builder.toString().split(String.format("\\%s",
                String.valueOf(DecimalFormatSymbols.getInstance(locale).getDecimalSeparator())));

        if (parts.length == 2) {
            int decimalStart = parts[0].length() + 1;
            int decimalEnd = decimalStart + fractionDigits;

            if (decimalEnd > builder.length()) {
                decimalEnd = decimalStart + parts[1].length();
            }

            builder.setSpan(new TopAlignSuperscriptSpan(), decimalStart, decimalEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }

    /**
     * Formats {@link Amount Amount(s)} within a string resource.
     *
     * @param context The current {@link Context}.
     * @param resId The string resource identifier.
     * @param formatArgs The format arguments. Any {@link Amount} objects will be formatted with {@link #format(Context, Amount)}.
     * @return The resource string with formatted {@link Amount Amount(s)}.
     */
    @NonNull
    public static CharSequence getStringWithFormattedAmounts(@NonNull Context context, @StringRes int resId, @Nullable Object... formatArgs) {
        Object[] convertedFormatArgs = formatArgs != null ? Arrays.copyOf(formatArgs, formatArgs.length) : null;

        if (convertedFormatArgs != null) {
            for (int i = 0; i < convertedFormatArgs.length; i++) {
                Object convertedFormatArg = convertedFormatArgs[i];

                if (convertedFormatArg instanceof Amount) {
                    convertedFormatArgs[i] = format(context, (Amount) convertedFormatArg);
                }
            }
        }

        return TextFormat.format(context, resId, convertedFormatArgs);
    }

    /**
     * Convert an {@link Amount} to its corresponding {@link BigDecimal} value.
     *
     * @param amount The {@link Amount} to be converted.
     * @return A {@link BigDecimal} representation of the {@link Amount}.
     */
    @NonNull
    public static BigDecimal toBigDecimal(@NonNull Amount amount) {
        return toBigDecimal(amount.getValue(), amount.getCurrency());
    }

    /**
     * Convert a value in minor units with the corresponding currency code to its {@link BigDecimal} representation.
     *
     * @param value The value in minor units.
     * @param currencyCode The currency code of the value.
     * @return A {@link BigDecimal} representation.
     */
    @NonNull
    public static BigDecimal toBigDecimal(long value, @NonNull String currencyCode) {
        int fractionDigits = getFractionDigits(currencyCode);

        return BigDecimal.valueOf(value, fractionDigits);
    }

    private static int getFractionDigits(@NonNull String currencyCode) {
        String normalizedCurrencyCode = currencyCode.replaceAll("[^A-Z]", "").toUpperCase(Locale.US);

        try {
            CheckoutCurrency checkoutCurrency = CheckoutCurrency.valueOf(normalizedCurrencyCode);

            return checkoutCurrency.getFractionDigits();
        } catch (IllegalArgumentException e) {
            Log.e(TAG, normalizedCurrencyCode + " is an unsupported currency. Falling back to information from java.util.Currency.");
        }

        try {
            Currency currency = Currency.getInstance(normalizedCurrencyCode);

            return Math.max(currency.getDefaultFractionDigits(), 0);
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Could not determine fraction digits for " + normalizedCurrencyCode, e);

            return 0;
        }
    }

    @NonNull
    private static String getCurrencySymbol(@NonNull Locale locale, @NonNull String currencyCode) {
        try {
            Currency currency = Currency.getInstance(currencyCode);
            String symbol = currency.getSymbol(locale);

            if (currencyCode.equals(symbol)) {
                throw new IllegalArgumentException("Missing currency symbol for " + currencyCode);
            }

            return symbol;
        } catch (IllegalArgumentException | NullPointerException e) {
            Log.e(TAG, "Could not determine currency symbol for " + currencyCode);

            return currencyCode;
        }
    }

    private AmountFormat() {
        throw new IllegalStateException("No instances.");
    }

    private static final class TopAlignSuperscriptSpan extends SuperscriptSpan {
        private static final float SCALE_PERCENTAGE = 0.7f;

        private static final float SHIFT_PERCENTAGE = 0.1f;

        @Override
        public void updateDrawState(@NonNull TextPaint textPaint) {
            updateState(textPaint);
        }

        @Override
        public void updateMeasureState(@NonNull TextPaint textPaint) {
            updateState(textPaint);
        }

        private void updateState(@NonNull TextPaint textPaint) {
            float ascent = textPaint.ascent();
            textPaint.setTextSize(textPaint.getTextSize() * SCALE_PERCENTAGE);
            float newAscent = textPaint.getFontMetrics().ascent;
            textPaint.baselineShift += (ascent - ascent * SHIFT_PERCENTAGE) - (newAscent - newAscent * SHIFT_PERCENTAGE);
        }
    }
}
