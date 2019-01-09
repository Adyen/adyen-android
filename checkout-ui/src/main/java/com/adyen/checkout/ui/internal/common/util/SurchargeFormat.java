/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 02/11/2018.
 */

package com.adyen.checkout.ui.internal.common.util;

import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;

import com.adyen.checkout.core.model.SurchargeConfiguration;
import com.adyen.checkout.util.AmountFormat;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public final class SurchargeFormat {

    private static final float SPAN_SIZE_PROPORTION = 0.875f;
    private static final double BASIS_POINTS_DIVIDER = 10_000d;

    @NonNull
    public static CharSequence format(@NonNull Context context, @NonNull SurchargeConfiguration surchargeConfiguration) {
        long fixedCost = surchargeConfiguration.getSurchargeFixedCost();
        int variableCost = surchargeConfiguration.getSurchargeVariableCost();

        CharSequence fixedAmount;
        CharSequence variablePercentage;

        SpannableStringBuilder surcharge = new SpannableStringBuilder();

        if (fixedCost > 0) {
            String currencyCode = surchargeConfiguration.getSurchargeCurrencyCode();
            fixedAmount = AmountFormat.format(context, fixedCost, currencyCode);
        } else {
            fixedAmount = null;
        }

        if (variableCost > 0) {
            NumberFormat percentFormat = DecimalFormat.getPercentInstance(getLocale(context));
            percentFormat.setMinimumFractionDigits(0);
            percentFormat.setMaximumFractionDigits(2);

            variablePercentage = percentFormat.format(variableCost / BASIS_POINTS_DIVIDER);
        } else {
            variablePercentage = null;
        }

        if (fixedAmount != null) {
            surcharge.append("(+")
                    .append(fixedAmount);

            if (variablePercentage == null) {
                surcharge.append(")");
            } else {
                surcharge.append(" ");
            }
        }

        if (variablePercentage != null) {
            if (fixedAmount == null) {
                surcharge.append("(");
            }

            surcharge.append("+")
                    .append(variablePercentage)
                    .append(")");
        }

        int length = surcharge.length();
        surcharge.setSpan(new RelativeSizeSpan(SPAN_SIZE_PROPORTION), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int foregroundColor = ThemeUtil.getAttributeColor(context, android.R.attr.textColorSecondary);
        surcharge.setSpan(new ForegroundColorSpan(foregroundColor), 0, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return surcharge;
    }

    @NonNull
    private static Locale getLocale(@NonNull Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            return context.getResources().getConfiguration().locale;
        } else {
            return context.getResources().getConfiguration().getLocales().get(0);
        }
    }

    private SurchargeFormat() {
        throw new IllegalStateException("No instances.");
    }
}
