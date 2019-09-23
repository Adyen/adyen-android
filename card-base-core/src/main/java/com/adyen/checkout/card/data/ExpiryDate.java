/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/9/2019.
 */

package com.adyen.checkout.card.data;

public class ExpiryDate {
    public static final int EMPTY_VALUE = 0;
    public static final ExpiryDate EMPTY_DATE = new ExpiryDate(EMPTY_VALUE, EMPTY_VALUE);

    private final int mExpiryMonth;
    private final int mExpiryYear;

    public ExpiryDate(int expiryMonth, int expiryYear) {
        mExpiryMonth = expiryMonth;
        mExpiryYear = expiryYear;
    }

    public int getExpiryMonth() {
        return mExpiryMonth;
    }

    public int getExpiryYear() {
        return mExpiryYear;
    }
}
