/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */

package com.adyen.checkout.card;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.InputData;
import com.adyen.checkout.card.data.ExpiryDate;

public final class CardInputData implements InputData {
    private String mCardNumber = "";
    private ExpiryDate mExpiryDate = ExpiryDate.EMPTY_DATE;
    private String mSecurityCode = "";
    private String mHolderName = "";
    private boolean mStorePayment;

    @NonNull
    public String getCardNumber() {
        return mCardNumber;
    }

    public void setCardNumber(@NonNull String cardNumber) {
        mCardNumber = cardNumber;
    }

    @NonNull
    public ExpiryDate getExpiryDate() {
        return mExpiryDate;
    }

    public void setExpiryDate(@NonNull ExpiryDate expiryDate) {
        mExpiryDate = expiryDate;
    }

    @NonNull
    public String getSecurityCode() {
        return mSecurityCode;
    }

    public void setSecurityCode(@NonNull String securityCode) {
        mSecurityCode = securityCode;
    }

    @NonNull
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
