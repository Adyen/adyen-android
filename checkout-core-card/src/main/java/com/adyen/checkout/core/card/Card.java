/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 31/08/2017.
 */

package com.adyen.checkout.core.card;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public final class Card implements Parcelable {
    @NonNull
    public static final Creator<Card> CREATOR = new Creator<Card>() {
        @Override
        public Card createFromParcel(Parcel source) {
            return new Card(source);
        }

        @Override
        public Card[] newArray(int size) {
            return new Card[size];
        }
    };

    private String mNumber;

    private Integer mExpiryMonth;

    private Integer mExpiryYear;

    private String mSecurityCode;

    private Card() {
        // Use builder.
    }

    private Card(@NonNull Parcel source) {
        mNumber = source.readString();
        mExpiryMonth = (Integer) source.readSerializable();
        mExpiryYear = (Integer) source.readSerializable();
        mSecurityCode = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mNumber);
        dest.writeSerializable(mExpiryMonth);
        dest.writeSerializable(mExpiryYear);
        dest.writeString(mSecurityCode);
    }

    @Nullable
    public String getNumber() {
        return mNumber;
    }

    @Nullable
    public Integer getExpiryMonth() {
        return mExpiryMonth;
    }

    @Nullable
    public Integer getExpiryYear() {
        return mExpiryYear;
    }

    @Nullable
    public String getSecurityCode() {
        return mSecurityCode;
    }

    /**
     * Builder for {@link Card}s.
     */
    public static final class Builder {
        private Card mCard = new Card();

        @NonNull
        public Builder setNumber(@Nullable String number) {
            mCard.mNumber = number;

            return this;
        }

        @NonNull
        public Builder setExpiryDate(int expiryMonth, int expiryYear) {
            mCard.mExpiryMonth = expiryMonth;
            mCard.mExpiryYear = expiryYear;

            return this;
        }

        @NonNull
        public Builder clearExpiryDate() {
            mCard.mExpiryMonth = null;
            mCard.mExpiryYear = null;

            return this;
        }

        @NonNull
        public Builder setSecurityCode(@Nullable String securityCode) {
            mCard.mSecurityCode = securityCode;

            return this;
        }

        @NonNull
        public Card build() {
            return mCard;
        }
    }
}
