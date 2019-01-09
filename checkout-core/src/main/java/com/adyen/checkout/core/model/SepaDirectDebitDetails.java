/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 16/08/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for SEPA direct debit payments.
 */
public final class SepaDirectDebitDetails extends PaymentMethodDetails {
    @NonNull
    public static final Creator<SepaDirectDebitDetails> CREATOR = new Creator<SepaDirectDebitDetails>() {
        @Override
        public SepaDirectDebitDetails createFromParcel(Parcel parcel) {
            return new SepaDirectDebitDetails(parcel);
        }

        @Override
        public SepaDirectDebitDetails[] newArray(int size) {
            return new SepaDirectDebitDetails[size];
        }
    };

    @NonNull
    public static final String KEY_SEPA_IBAN_NUMBER = "sepa.ibanNumber";

    @NonNull
    public static final String KEY_SEPA_OWNER_NAME = "sepa.ownerName";

    private String mSepaIbanNumber;

    private String mSepaOwnerName;

    private SepaDirectDebitDetails() {
        // Empty constructor for Builder.
    }

    private SepaDirectDebitDetails(@NonNull Parcel in) {
        super(in);

        mSepaIbanNumber = in.readString();
        mSepaOwnerName = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(mSepaIbanNumber);
        parcel.writeString(mSepaOwnerName);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_SEPA_IBAN_NUMBER, mSepaIbanNumber);
        jsonObject.put(KEY_SEPA_OWNER_NAME, mSepaOwnerName);
        return jsonObject;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SepaDirectDebitDetails that = (SepaDirectDebitDetails) o;

        if (mSepaIbanNumber != null ? !mSepaIbanNumber.equals(that.mSepaIbanNumber) : that.mSepaIbanNumber != null) {
            return false;
        }
        return mSepaOwnerName != null ? mSepaOwnerName.equals(that.mSepaOwnerName) : that.mSepaOwnerName == null;
    }

    @Override
    public int hashCode() {
        int result = mSepaIbanNumber != null ? mSepaIbanNumber.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mSepaOwnerName != null ? mSepaOwnerName.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private final SepaDirectDebitDetails mSepaDirectDebitDetails;

        public Builder(@NonNull String sepaIbanNumber, @NonNull String sepaOwnerName) {
            mSepaDirectDebitDetails = new SepaDirectDebitDetails();
            mSepaDirectDebitDetails.mSepaIbanNumber = sepaIbanNumber;
            mSepaDirectDebitDetails.mSepaOwnerName = sepaOwnerName;
        }

        @NonNull
        public SepaDirectDebitDetails build() {
            return mSepaDirectDebitDetails;
        }
    }
}
