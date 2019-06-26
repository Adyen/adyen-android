/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card.data.formatter;

import android.support.annotation.NonNull;

public final class CardFormatterImpl implements CardFormatter {
    private final NumberFormatter mNumberFormatter;
    private final ExpiryDateFormatter mExpiryDateFormatter;
    private final SecurityCodeFormatter mSecurityCodeFormatter;

    CardFormatterImpl(@NonNull NumberFormatter numberFormatter, @NonNull ExpiryDateFormatter expiryDateFormatter,
            @NonNull SecurityCodeFormatter securityCodeFormatter) {
        mNumberFormatter = numberFormatter;
        mExpiryDateFormatter = expiryDateFormatter;
        mSecurityCodeFormatter = securityCodeFormatter;
    }

    @NonNull
    @Override
    public String unformatNumber(@NonNull String number) {
        return mNumberFormatter.unformatNumber(number);
    }

    @NonNull
    @Override
    public String formatNumber(@NonNull String number) {
        return mNumberFormatter.formatNumber(number);
    }

    @NonNull
    @Override
    public String maskNumber(@NonNull String number) {
        return mNumberFormatter.maskNumber(number);
    }

    @NonNull
    @Override
    public String formatExpiryDate(int expiryMonth, int expiryYear) {
        return mExpiryDateFormatter.formatExpiryDate(expiryMonth, expiryYear);
    }

    @NonNull
    @Override
    public String formatExpiryDate(@NonNull String expiryDate, @NonNull String prevExpiryDate) {
        return mExpiryDateFormatter.formatExpiryDate(expiryDate, prevExpiryDate);
    }

    @NonNull
    @Override
    public String formatSecurityCode(@NonNull String securityCode) {
        return mSecurityCodeFormatter.formatSecurityCode(securityCode);
    }
}
