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

import com.adyen.checkout.base.internal.Parcelables;
import com.adyen.checkout.base.internal.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link PaymentMethodDetails} for credit card payments. This class contains only tokenized and encrypted card data.
 */
public final class CardDetails extends PaymentMethodDetails {
    @NonNull
    public static final Creator<CardDetails> CREATOR = new Creator<CardDetails>() {
        @Override
        public CardDetails createFromParcel(Parcel parcel) {
            return new CardDetails(parcel);
        }

        @Override
        public CardDetails[] newArray(int size) {
            return new CardDetails[size];
        }
    };

    @NonNull
    public static final String KEY_HOLDER_NAME = "holderName";

    @NonNull
    public static final String KEY_ENCRYPTED_CARD_NUMBER = "encryptedCardNumber";

    @NonNull
    public static final String KEY_ENCRYPTED_EXPIRY_MONTH = "encryptedExpiryMonth";

    @NonNull
    public static final String KEY_ENCRYPTED_EXPIRY_YEAR = "encryptedExpiryYear";

    @NonNull
    public static final String KEY_ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode";

    @NonNull
    public static final String KEY_PHONE_NUMBER = "telephoneNumber";

    @NonNull
    public static final String KEY_STORE_DETAILS = "storeDetails";

    @NonNull
    public static final String KEY_INSTALLMENTS = "installments";

    @NonNull
    public static final String KEY_BILLING_ADDRESS = "billingAddress";

    private String mHolderName;

    private String mEncryptedCardNumber;

    private String mEncryptedExpiryMonth;

    private String mEncryptedExpiryYear;

    private String mEncryptedSecurityCode;

    private String mPhoneNumber;

    private Boolean mStoreDetails;

    private Integer mInstallments;

    private Address mBillingAddress;

    private CardDetails() {
        // Empty constructor for Builder.
    }

    private CardDetails(@NonNull Parcel in) {
        super(in);

        mHolderName = in.readString();
        mEncryptedCardNumber = in.readString();
        mEncryptedExpiryMonth = in.readString();
        mEncryptedExpiryYear = in.readString();
        mEncryptedSecurityCode = in.readString();
        mPhoneNumber = in.readString();
        mStoreDetails = Parcelables.readSerializable(in);
        mInstallments = Parcelables.readSerializable(in);
        mBillingAddress = Parcelables.read(in, Address.class);
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(mHolderName);
        parcel.writeString(mEncryptedCardNumber);
        parcel.writeString(mEncryptedExpiryMonth);
        parcel.writeString(mEncryptedExpiryYear);
        parcel.writeString(mEncryptedSecurityCode);
        parcel.writeString(mPhoneNumber);
        Parcelables.writeSerializable(parcel, mStoreDetails);
        Parcelables.writeSerializable(parcel, mInstallments);
        Parcelables.write(parcel, mBillingAddress);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_HOLDER_NAME, mHolderName);
        jsonObject.put(KEY_ENCRYPTED_CARD_NUMBER, mEncryptedCardNumber);
        jsonObject.put(KEY_ENCRYPTED_EXPIRY_MONTH, mEncryptedExpiryMonth);
        jsonObject.put(KEY_ENCRYPTED_EXPIRY_YEAR, mEncryptedExpiryYear);
        jsonObject.put(KEY_ENCRYPTED_SECURITY_CODE, mEncryptedSecurityCode);
        jsonObject.put(KEY_PHONE_NUMBER, mPhoneNumber);
        jsonObject.put(KEY_STORE_DETAILS, mStoreDetails);
        jsonObject.put(KEY_INSTALLMENTS, mInstallments);
        jsonObject.put(KEY_BILLING_ADDRESS, mBillingAddress != null ? mBillingAddress.serialize() : null);

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

        CardDetails that = (CardDetails) o;

        if (mHolderName != null ? !mHolderName.equals(that.mHolderName) : that.mHolderName != null) {
            return false;
        }
        if (mEncryptedCardNumber != null ? !mEncryptedCardNumber.equals(that.mEncryptedCardNumber) : that.mEncryptedCardNumber != null) {
            return false;
        }
        if (mEncryptedExpiryMonth != null ? !mEncryptedExpiryMonth.equals(that.mEncryptedExpiryMonth) : that.mEncryptedExpiryMonth != null) {
            return false;
        }
        if (mEncryptedExpiryYear != null ? !mEncryptedExpiryYear.equals(that.mEncryptedExpiryYear) : that.mEncryptedExpiryYear != null) {
            return false;
        }
        if (mEncryptedSecurityCode != null ? !mEncryptedSecurityCode.equals(that.mEncryptedSecurityCode) : that.mEncryptedSecurityCode != null) {
            return false;
        }
        if (mPhoneNumber != null ? !mPhoneNumber.equals(that.mPhoneNumber) : that.mPhoneNumber != null) {
            return false;
        }
        if (mStoreDetails != null ? !mStoreDetails.equals(that.mStoreDetails) : that.mStoreDetails != null) {
            return false;
        }
        if (mInstallments != null ? !mInstallments.equals(that.mInstallments) : that.mInstallments != null) {
            return false;
        }
        return mBillingAddress != null ? mBillingAddress.equals(that.mBillingAddress) : that.mBillingAddress == null;
    }

    @Override
    public int hashCode() {
        int result = mHolderName != null ? mHolderName.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mEncryptedCardNumber != null ? mEncryptedCardNumber.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mEncryptedExpiryMonth != null ? mEncryptedExpiryMonth.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mEncryptedExpiryYear != null ? mEncryptedExpiryYear.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mEncryptedSecurityCode != null ? mEncryptedSecurityCode.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPhoneNumber != null ? mPhoneNumber.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mStoreDetails != null ? mStoreDetails.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mInstallments != null ? mInstallments.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mBillingAddress != null ? mBillingAddress.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private final CardDetails mCardDetails;

        public Builder() {
            mCardDetails = new CardDetails();
        }

        @NonNull
        public Builder setHolderName(@Nullable String holderName) {
            mCardDetails.mHolderName = holderName;

            return this;
        }

        @NonNull
        public Builder setEncryptedCardNumber(@Nullable String encryptedCardNumber) {
            mCardDetails.mEncryptedCardNumber = encryptedCardNumber;

            return this;
        }

        @NonNull
        public Builder setEncryptedExpiryMonth(@Nullable String encryptedExpiryMonth) {
            mCardDetails.mEncryptedExpiryMonth = encryptedExpiryMonth;

            return this;
        }

        @NonNull
        public Builder setEncryptedExpiryYear(@Nullable String encryptedExpiryYear) {
            mCardDetails.mEncryptedExpiryYear = encryptedExpiryYear;

            return this;
        }

        @NonNull
        public Builder setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
            mCardDetails.mEncryptedSecurityCode = encryptedSecurityCode;

            return this;
        }

        @NonNull
        public Builder setPhoneNumber(@Nullable String phoneNumber) {
            mCardDetails.mPhoneNumber = phoneNumber;

            return this;
        }

        @NonNull
        public Builder setStoreDetails(@Nullable Boolean storeDetails) {
            mCardDetails.mStoreDetails = storeDetails;

            return this;
        }

        @NonNull
        public Builder setInstallments(@Nullable Integer installments) {
            mCardDetails.mInstallments = installments;

            return this;
        }

        @NonNull
        public Builder setBillingAddress(@Nullable Address billingAddress) {
            mCardDetails.mBillingAddress = billingAddress;

            return this;
        }

        @Nullable
        public CardDetails build() {
            try {
                JSONObject serialized = mCardDetails.serialize();

                if (serialized.names().length() == 0) {
                    return null;
                } else {
                    return mCardDetails;
                }
            } catch (JSONException e) {
                throw new RuntimeException("Invalid JSON.");
            }
        }
    }
}
