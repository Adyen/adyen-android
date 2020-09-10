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

@SuppressWarnings("SyntheticAccessor")
public final class EncryptedCard implements Parcelable {
    @NonNull
    public static final Parcelable.Creator<EncryptedCard> CREATOR = new Creator<EncryptedCard>() {
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
        return ParcelUtils.NO_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
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
        private final EncryptedCard mEncryptedCard = new EncryptedCard();

        /**
         * Set encrypted number.
         *
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedNumber(@Nullable String encryptedNumber) {
            mEncryptedCard.mEncryptedNumber = encryptedNumber;

            return this;
        }

        /**
         * Set encrypted expiry date.
         *
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedExpiryDate(@NonNull String encryptedExpiryMonth, @NonNull String encryptedExpiryYear) {
            mEncryptedCard.mEncryptedExpiryMonth = encryptedExpiryMonth;
            mEncryptedCard.mEncryptedExpiryYear = encryptedExpiryYear;

            return this;
        }

        /**
         * Clear expiry date.
         *
         * @return {@link EncryptedCard.Builder}
         */
        @SuppressWarnings("PMD.NullAssignment")
        @NonNull
        public Builder clearEncryptedExpiryDate() {
            mEncryptedCard.mEncryptedExpiryMonth = null;
            mEncryptedCard.mEncryptedExpiryYear = null;

            return this;
        }

        /**
         * Set encrypted security code.
         *
         * @return {@link EncryptedCard.Builder}
         */
        @NonNull
        public Builder setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
            mEncryptedCard.mEncryptedSecurityCode = encryptedSecurityCode;

            return this;
        }

        /**
         * Build EncryptedCard object.
         *
         * @return {@link EncryptedCard}
         */
        @NonNull
        public EncryptedCard build() {
            return mEncryptedCard;
        }
    }
}
