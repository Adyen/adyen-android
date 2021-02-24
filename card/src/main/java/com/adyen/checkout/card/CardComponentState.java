/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 21/10/2019.
 */

package com.adyen.checkout.card;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.PaymentComponentState;
import com.adyen.checkout.components.model.payments.request.CardPaymentMethod;
import com.adyen.checkout.components.model.payments.request.PaymentComponentData;
import com.adyen.checkout.card.data.CardType;

public class CardComponentState extends PaymentComponentState<CardPaymentMethod> {

    private final CardType mCardType;
    private final String mBinValue;
    private final String mLastFourDigits;

    /**
     * PaymentComponentState for CardComponent with additional data.
     */
    public CardComponentState(
            @NonNull PaymentComponentData<CardPaymentMethod> paymentComponentData,
            boolean isValid,
            @Nullable CardType cardType,
            @NonNull String binValue,
            @Nullable String lastFourDigits) {
        super(paymentComponentData, isValid);
        this.mBinValue = binValue;
        this.mCardType = cardType;
        this.mLastFourDigits = lastFourDigits;
    }

    @Nullable
    public CardType getCardType() {
        return mCardType;
    }

    @NonNull
    public String getBinValue() {
        return mBinValue;
    }

    @NonNull
    public String getLastFourDigits() {
        return mLastFourDigits;
    }
}
