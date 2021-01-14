/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 6/6/2019.
 */

package com.adyen.checkout.cse;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.util.ParcelUtils;

public final class UnencryptedCard implements Parcelable {
    @NonNull
    public static final Creator<UnencryptedCard> CREATOR = new Creator<UnencryptedCard>() {
        @Override
        public UnencryptedCard createFromParcel(Parcel source) {
            return new UnencryptedCard(source);
        }

        @Override
        public UnencryptedCard[] newArray(int size) {
            return new UnencryptedCard[size];
        }
    };

    private String mNumber;

    private Integer mExpiryMonth;

    private Integer mExpiryYear;

    private String mSecurityCode;

    private UnencryptedCard() {
        // Use builder.
    }

    private UnencryptedCard(@NonNull Parcel source) {
        mNumber = source.readString();
        mExpiryMonth = (Integer) source.readSerializable();
        mExpiryYear = (Integer) source.readSerializable();
        mSecurityCode = source.readString();
    }

    @Override
    public int describeContents() {
        return ParcelUtils.NO_FILE_DESCRIPTOR;
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
     * Builder for {@link UnencryptedCard}s.
     */
    public static final class Builder {
        private final UnencryptedCard mUnencryptedCard = new UnencryptedCard();

        /**
         * Set number of card.
         *
         * @return {@link UnencryptedCard.Builder}
         */
        @NonNull
        public Builder setNumber(@Nullable String number) {
            mUnencryptedCard.mNumber = number;

            return this;
        }

        /**
         * Set expiry date.
         *
         * @return {@link UnencryptedCard.Builder}
         */
        @NonNull
        public Builder setExpiryDate(int expiryMonth, int expiryYear) {
            mUnencryptedCard.mExpiryMonth = expiryMonth;
            mUnencryptedCard.mExpiryYear = expiryYear;

            return this;
        }

        /**
         * Set security code.
         *
         * @return {@link UnencryptedCard.Builder}
         */
        @NonNull
        public Builder setSecurityCode(@Nullable String securityCode) {
            mUnencryptedCard.mSecurityCode = securityCode;

            return this;
        }

        /**
         * Build card object.
         *
         * @return {@link UnencryptedCard}
         */
        @NonNull
        public UnencryptedCard build() {
            return mUnencryptedCard;
        }
    }
}
