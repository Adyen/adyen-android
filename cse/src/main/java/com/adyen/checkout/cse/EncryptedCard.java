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

    private final String mEncryptedCardNumber;
    private final String mEncryptedExpiryMonth;
    private final String mEncryptedExpiryYear;
    private final String mEncryptedSecurityCode;

    EncryptedCard(
            @Nullable String encryptedCardNumber,
            @Nullable String encryptedExpiryMonth,
            @Nullable String encryptedExpiryYear,
            @Nullable String encryptedSecurityCode
    ) {
        mEncryptedCardNumber = encryptedCardNumber;
        mEncryptedExpiryMonth = encryptedExpiryMonth;
        mEncryptedExpiryYear = encryptedExpiryYear;
        mEncryptedSecurityCode = encryptedSecurityCode;
    }

    private EncryptedCard(@NonNull Parcel source) {
        mEncryptedCardNumber = source.readString();
        mEncryptedExpiryMonth = source.readString();
        mEncryptedExpiryYear = source.readString();
        mEncryptedSecurityCode = source.readString();
    }

    @Override
    public int describeContents() {
        return Parcelable.CONTENTS_FILE_DESCRIPTOR;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(mEncryptedCardNumber);
        dest.writeString(mEncryptedExpiryMonth);
        dest.writeString(mEncryptedExpiryYear);
        dest.writeString(mEncryptedSecurityCode);
    }

    @Nullable
    public String getEncryptedCardNumber() {
        return mEncryptedCardNumber;
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

}
