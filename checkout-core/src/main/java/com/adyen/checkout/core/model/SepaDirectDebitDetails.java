package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for SEPA direct debit payments.
 * <p>
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 16/08/2017.
 */
public final class SepaDirectDebitDetails extends PaymentMethodDetails {
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

    public static final String KEY_SEPA_IBAN_NUMBER = "sepa.ibanNumber";

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
    public void writeToParcel(Parcel parcel, int flags) {
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
    public boolean equals(Object o) {
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
        result = 31 * result + (mSepaOwnerName != null ? mSepaOwnerName.hashCode() : 0);
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
