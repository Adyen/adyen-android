package com.adyen.checkout.core.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonSerializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/08/2017.
 */
public final class Address implements Parcelable, JsonSerializable {
    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel parcel) {
            return new Address(parcel);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    private String mStreet;

    private String mHouseNumberOrName;

    private String mCity;

    private String mCountry;

    private String mPostalCode;

    private String mStateOrProvince;

    private Address() {
        // Empty constructor for Builder.
    }

    private Address(@NonNull Parcel in) {
        mStreet = in.readString();
        mHouseNumberOrName = in.readString();
        mCity = in.readString();
        mCountry = in.readString();
        mPostalCode = in.readString();
        mStateOrProvince = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int flags) {
        parcel.writeString(mStreet);
        parcel.writeString(mHouseNumberOrName);
        parcel.writeString(mCity);
        parcel.writeString(mCountry);
        parcel.writeString(mPostalCode);
        parcel.writeString(mStateOrProvince);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("street", mStreet);
        jsonObject.put("houseNumberOrName", mHouseNumberOrName);
        jsonObject.put("city", mCity);
        jsonObject.put("country", mCountry);
        jsonObject.put("postalCode", mPostalCode);
        jsonObject.put("stateOrProvince", mStateOrProvince);
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

        Address address = (Address) o;

        if (mStreet != null ? !mStreet.equals(address.mStreet) : address.mStreet != null) {
            return false;
        }
        if (mHouseNumberOrName != null ? !mHouseNumberOrName.equals(address.mHouseNumberOrName) : address.mHouseNumberOrName != null) {
            return false;
        }
        if (mCity != null ? !mCity.equals(address.mCity) : address.mCity != null) {
            return false;
        }
        if (mCountry != null ? !mCountry.equals(address.mCountry) : address.mCountry != null) {
            return false;
        }
        if (mPostalCode != null ? !mPostalCode.equals(address.mPostalCode) : address.mPostalCode != null) {
            return false;
        }
        return mStateOrProvince != null ? mStateOrProvince.equals(address.mStateOrProvince) : address.mStateOrProvince == null;
    }

    @Override
    public int hashCode() {
        int result = mStreet != null ? mStreet.hashCode() : 0;
        result = 31 * result + (mHouseNumberOrName != null ? mHouseNumberOrName.hashCode() : 0);
        result = 31 * result + (mCity != null ? mCity.hashCode() : 0);
        result = 31 * result + (mCountry != null ? mCountry.hashCode() : 0);
        result = 31 * result + (mPostalCode != null ? mPostalCode.hashCode() : 0);
        result = 31 * result + (mStateOrProvince != null ? mStateOrProvince.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private Address mAddress;

        public Builder(
                @NonNull String street,
                @NonNull String houseNumberOrName,
                @NonNull String city,
                @NonNull String country,
                @NonNull String postalCode
        ) {
            mAddress = new Address();
            mAddress.mStreet = street;
            mAddress.mHouseNumberOrName = houseNumberOrName;
            mAddress.mCity = city;
            mAddress.mCountry = country;
            mAddress.mPostalCode = postalCode;
        }

        @NonNull
        public Builder setStateOrProvince(@Nullable String stateOrProvince) {
            mAddress.mStateOrProvince = stateOrProvince;

            return this;
        }

        @NonNull
        public Address build() {
            return mAddress;
        }
    }
}
