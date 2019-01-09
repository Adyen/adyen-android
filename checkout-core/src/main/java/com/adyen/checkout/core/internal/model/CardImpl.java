/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.Card;

import org.json.JSONException;
import org.json.JSONObject;

public final class CardImpl extends JsonObject implements Card {
    @NonNull
    public static final Parcelable.Creator<CardImpl> CREATOR = new DefaultCreator<>(CardImpl.class);

    private String mHolderName;

    private Integer mExpiryMonth;

    private Integer mExpiryYear;

    private String mNumber;

    private CardImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mHolderName = jsonObject.getString("holderName");
        mExpiryMonth = jsonObject.getInt("expiryMonth");
        mExpiryYear = jsonObject.getInt("expiryYear");
        mNumber = jsonObject.getString("number");
    }

    @NonNull
    @Override
    public String getHolderName() {
        return mHolderName;
    }

    @Override
    public int getExpiryMonth() {
        return mExpiryMonth;
    }

    @Override
    public int getExpiryYear() {
        return mExpiryYear;
    }

    @NonNull
    @Override
    public String getLastFourDigits() {
        return mNumber;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardImpl card = (CardImpl) o;

        if (mHolderName != null ? !mHolderName.equals(card.mHolderName) : card.mHolderName != null) {
            return false;
        }
        if (mExpiryMonth != null ? !mExpiryMonth.equals(card.mExpiryMonth) : card.mExpiryMonth != null) {
            return false;
        }
        if (mExpiryYear != null ? !mExpiryYear.equals(card.mExpiryYear) : card.mExpiryYear != null) {
            return false;
        }
        return mNumber != null ? mNumber.equals(card.mNumber) : card.mNumber == null;
    }

    @Override
    public int hashCode() {
        int result = mHolderName != null ? mHolderName.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mExpiryMonth != null ? mExpiryMonth.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mExpiryYear != null ? mExpiryYear.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mNumber != null ? mNumber.hashCode() : 0);
        return result;
    }

    @NonNull
    @Override
    public String toString() {
        return "Card{" + "Number='" + mNumber + '\'' + '}';
    }
}
