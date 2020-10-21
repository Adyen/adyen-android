/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/7/2019.
 */

package com.adyen.checkout.base.util;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.model.payments.Amount;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Locale;

public final class AmountFormat {
    private static final String TAG = LogUtil.getTag();

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
        final int fractionDigits = getFractionDigits(currencyCode);

        return BigDecimal.valueOf(value, fractionDigits);
    }

    private static int getFractionDigits(@NonNull String currencyCode) {
        final String normalizedCurrencyCode = currencyCode.replaceAll("[^A-Z]", "").toUpperCase(Locale.ROOT);

        try {
            final CheckoutCurrency checkoutCurrency = CheckoutCurrency.find(normalizedCurrencyCode);
            return checkoutCurrency.getFractionDigits();
        } catch (CheckoutException e) {
            Logger.e(TAG, normalizedCurrencyCode + " is an unsupported currency. Falling back to information from java.util.Currency.", e);
        }

        try {
            final Currency currency = Currency.getInstance(normalizedCurrencyCode);
            return Math.max(currency.getDefaultFractionDigits(), 0);
        } catch (IllegalArgumentException e) {
            Logger.e(TAG, "Could not determine fraction digits for " + normalizedCurrencyCode, e);
            return 0;
        }
    }

    private AmountFormat() {
        throw new NoConstructorException();
    }
}
