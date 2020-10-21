/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/9/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.model.payments.Amount;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.log.LogUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

public final class CurrencyUtils {
    public static final String TAG = LogUtil.getTag();

    /**
     * Format the {@link Amount} to be displayed to the user based on the Locale.
     *
     * @param amount The amount with currency and value.
     * @param locale The locale the amount will be formatted with.
     * @return A formatted string displaying currency and value.
     */
    @NonNull
    public static String formatAmount(@NonNull Amount amount, @NonNull Locale locale) {

        final String currencyCode = amount.getCurrency();
        final CheckoutCurrency checkoutCurrency = CheckoutCurrency.find(currencyCode);

        final Currency currency = Currency.getInstance(currencyCode);
        final NumberFormat currencyFormat = DecimalFormat.getCurrencyInstance(locale);
        currencyFormat.setCurrency(currency);
        currencyFormat.setMinimumFractionDigits(checkoutCurrency.getFractionDigits());
        currencyFormat.setMaximumFractionDigits(checkoutCurrency.getFractionDigits());

        final BigDecimal value = BigDecimal.valueOf(amount.getValue(), checkoutCurrency.getFractionDigits());
        return currencyFormat.format(value);
    }

    static void assertCurrency(@Nullable String currencyCode) {
        if (!CheckoutCurrency.isSupported(currencyCode)) {
            throw new CheckoutException("Currency " + currencyCode + " not supported");
        }
    }

    private CurrencyUtils() {
        throw new NoConstructorException();
    }
}
