/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc.data;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.InputData;
import com.adyen.checkout.card.data.ExpiryDate;

public final class BcmcInputData implements InputData {
    private String mCardNumber = "";
    private ExpiryDate mExpiryDate = ExpiryDate.EMPTY_DATE;

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
}
