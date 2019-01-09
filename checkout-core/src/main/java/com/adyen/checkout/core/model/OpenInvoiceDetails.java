/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/10/2018.
 */

package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.base.internal.Parcelables;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class OpenInvoiceDetails extends PaymentMethodDetails {
    @NonNull
    public static final String KEY_PERSONAL_DETAILS = "personalDetails";
    @NonNull
    public static final String KEY_BILLING_ADDRESS = "billingAddress";
    @NonNull
    public static final String KEY_SEPARATE_DELIVERY_ADDRESS = "separateDeliveryAddress";
    @NonNull
    public static final String KEY_DELIVERY_ADDRESS = "deliveryAddress";
    @NonNull
    public static final String KEY_CONSENT_CHECKBOX = "consentCheckbox";

    PersonalDetails mPersonalDetails;
    Address mBillingAddress;
    Boolean mSeparateDeliveryAddress;
    Address mDeliveryAddress;
    Boolean mConsentCheckbox;

    OpenInvoiceDetails() {
        // Empty constructor for Builder.
    }

    OpenInvoiceDetails(@NonNull Parcel in) {
        mPersonalDetails = Parcelables.read(in, PersonalDetails.class);
        mBillingAddress = Parcelables.read(in, Address.class);
        mSeparateDeliveryAddress = Parcelables.readSerializable(in);
        mDeliveryAddress = Parcelables.read(in, Address.class);
        mConsentCheckbox = Parcelables.readSerializable(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        Parcelables.write(parcel, mPersonalDetails);
        Parcelables.write(parcel, mBillingAddress);
        Parcelables.writeSerializable(parcel, mSeparateDeliveryAddress);
        Parcelables.write(parcel, mDeliveryAddress);
        Parcelables.writeSerializable(parcel, mConsentCheckbox);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_PERSONAL_DETAILS, mPersonalDetails != null ? mPersonalDetails.serialize() : null);
        jsonObject.put(KEY_BILLING_ADDRESS, mBillingAddress != null ? mBillingAddress.serialize() : null);
        jsonObject.put(KEY_SEPARATE_DELIVERY_ADDRESS, mSeparateDeliveryAddress);
        jsonObject.put(KEY_DELIVERY_ADDRESS, mDeliveryAddress != null ? mDeliveryAddress.serialize() : null);
        jsonObject.put(KEY_CONSENT_CHECKBOX, mConsentCheckbox);

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

        OpenInvoiceDetails that = (OpenInvoiceDetails) o;

        if (mPersonalDetails != null ? !mPersonalDetails.equals(that.mPersonalDetails) : that.mPersonalDetails != null) {
            return false;
        }
        if (mBillingAddress != null ? !mBillingAddress.equals(that.mBillingAddress) : that.mBillingAddress != null) {
            return false;
        }
        if (mSeparateDeliveryAddress != null ? !mSeparateDeliveryAddress.equals(that.mSeparateDeliveryAddress)
                : that.mSeparateDeliveryAddress != null) {
            return false;
        }
        if (mDeliveryAddress != null ? !mDeliveryAddress.equals(that.mDeliveryAddress) : that.mDeliveryAddress != null) {
            return false;
        }
        return mConsentCheckbox != null ? mConsentCheckbox.equals(that.mConsentCheckbox) : that.mConsentCheckbox == null;
    }

    @Override
    public int hashCode() {
        int result = mPersonalDetails != null ? mPersonalDetails.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mBillingAddress != null ? mBillingAddress.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mSeparateDeliveryAddress != null ? mSeparateDeliveryAddress.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mDeliveryAddress != null ? mDeliveryAddress.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mConsentCheckbox != null ? mConsentCheckbox.hashCode() : 0);
        return result;
    }
}
