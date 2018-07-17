package com.adyen.checkout.core.card;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 06/02/2018.
 */
public final class EncryptedCard implements Parcelable {
    public static final Parcelable.Creator<EncryptedCard> CREATOR = new Parcelable.Creator<EncryptedCard>() {
        @Override
        public EncryptedCard createFromParcel(Parcel source) {
            return new EncryptedCard(source);
        }

        @Override
        public EncryptedCard[] newArray(int size) {
            return new EncryptedCard[size];
        }
    };

    private String mEncryptedNumber;

    private String mEncryptedExpiryMonth;

    private String mEncryptedExpiryYear;

    private String mEncryptedSecurityCode;

    private EncryptedCard() {
        // Use builder.
    }

    private EncryptedCard(@NonNull Parcel source) {
        mEncryptedNumber = source.readString();
        mEncryptedExpiryMonth = source.readString();
        mEncryptedExpiryYear = source.readString();
        mEncryptedSecurityCode = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mEncryptedNumber);
        dest.writeString(mEncryptedExpiryMonth);
        dest.writeString(mEncryptedExpiryYear);
        dest.writeString(mEncryptedSecurityCode);
    }

    @Nullable
    public String getEncryptedNumber() {
        return mEncryptedNumber;
    }

    @Nullable
    public String getEncryptedExpiryMonth() {
        return mEncryptedExpiryMonth;
    }

    @Nullable
    public String getEncryptedExpiryYear() {
        return mEncryptedExpiryYear;
    }

    @Nullable
    public String getEncryptedSecurityCode() {
        return mEncryptedSecurityCode;
    }

    /**
     * Builder for {@link EncryptedCard}s.
     */
    public static final class Builder {
        private EncryptedCard mEncryptedCard = new EncryptedCard();

        @NonNull
        public Builder setEncryptedNumber(@Nullable String encryptedNumber) {
            mEncryptedCard.mEncryptedNumber = encryptedNumber;

            return this;
        }

        @NonNull
        public Builder setEncryptedExpiryDate(@NonNull String encryptedExpiryMonth, @NonNull String encryptedExpiryYear) {
            mEncryptedCard.mEncryptedExpiryMonth = encryptedExpiryMonth;
            mEncryptedCard.mEncryptedExpiryYear = encryptedExpiryYear;

            return this;
        }

        @NonNull
        public Builder clearEncryptedExpiryDate() {
            mEncryptedCard.mEncryptedExpiryMonth = null;
            mEncryptedCard.mEncryptedExpiryYear = null;

            return this;
        }

        @NonNull
        public Builder setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
            mEncryptedCard.mEncryptedSecurityCode = encryptedSecurityCode;

            return this;
        }

        @NonNull
        public EncryptedCard build() {
            return mEncryptedCard;
        }
    }
}
