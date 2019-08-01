/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.card.data.input;

import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.data.input.InputData;

public final class CardInputData implements InputData {
    private String mCardNumber = "";
    private String mExpiryDate = "";
    private String mSecurityCode = "";
    private String mHolderName = "";
    private boolean mStorePayment;

    @Nullable
    public String getCardNumber() {
        return mCardNumber;
    }

    public void setCardNumber(@Nullable String cardNumber) {
        mCardNumber = cardNumber;
    }

    @Nullable
    public String getExpiryDate() {
        return mExpiryDate;
    }

    public void setExpiryDate(@Nullable String expiryDate) {
        mExpiryDate = expiryDate;
    }

    @Nullable
    public String getSecurityCode() {
        return mSecurityCode;
    }

    public void setSecurityCode(@Nullable String securityCode) {
        mSecurityCode = securityCode;
    }

    @Nullable
    public String getHolderName() {
        return mHolderName;
    }

    public void setHolderName(@Nullable String holderName) {
        mHolderName = holderName;
    }

    public boolean isStorePaymentEnable() {
        return mStorePayment;
    }

    public void setStorePayment(boolean storePayment) {
        mStorePayment = storePayment;
    }
}
