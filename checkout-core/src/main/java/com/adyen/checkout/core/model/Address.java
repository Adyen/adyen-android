/*
 * Copyright (c) 2017 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/08/2017.
 */

package com.adyen.checkout.core.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.JsonSerializable;
import com.adyen.checkout.base.internal.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class Address extends JsonObject implements JsonSerializable {
    @NonNull
    public static final Parcelable.Creator<Address> CREATOR = new DefaultCreator<>(Address.class);

    @NonNull
    public static final String KEY_STREET = "street";

    @NonNull
    public static final String KEY_HOUSE_NUMBER_OR_NAME = "houseNumberOrName";

    @NonNull
    public static final String KEY_CITY = "city";

    @NonNull
    public static final String KEY_COUNTRY = "country";

    @NonNull
    public static final String KEY_POSTAL_CODE = "postalCode";

    @NonNull
    public static final String KEY_STATE_OR_PROVINCE = "stateOrProvince";

    private String mStreet;

    private String mHouseNumberOrName;

    private String mCity;

    private String mCountry;

    private String mPostalCode;

    private String mStateOrProvince;

    protected Address(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mCity = jsonObject.optString(KEY_CITY);
        mCountry = jsonObject.optString(KEY_COUNTRY);
        mHouseNumberOrName = jsonObject.optString(KEY_HOUSE_NUMBER_OR_NAME);
        mPostalCode = jsonObject.optString(KEY_POSTAL_CODE);
        mStreet = jsonObject.optString(KEY_STREET);
    }

    @NonNull
    @Override
    public JSONObject serialize() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_STREET, mStreet);
        jsonObject.put(KEY_HOUSE_NUMBER_OR_NAME, mHouseNumberOrName);
        jsonObject.put(KEY_CITY, mCity);
        jsonObject.put(KEY_COUNTRY, mCountry);
        jsonObject.put(KEY_POSTAL_CODE, mPostalCode);
        jsonObject.put(KEY_STATE_OR_PROVINCE, mStateOrProvince);
        return jsonObject;
    }

    @NonNull
    public String getStreet() {
        return mStreet;
    }

    @NonNull
    public String getHouseNumberOrName() {
        return mHouseNumberOrName;
    }

    @NonNull
    public String getCity() {
        return mCity;
    }

    @NonNull
    public String getCountry() {
        return mCountry;
    }

    @NonNull
    public String getPostalCode() {
        return mPostalCode;
    }

    @Nullable
    public String getStateOrProvince() {
        return mStateOrProvince;
    }

    @Override
    public boolean equals(@Nullable Object o) {
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
        result = HashUtils.MULTIPLIER * result + (mHouseNumberOrName != null ? mHouseNumberOrName.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mCity != null ? mCity.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mCountry != null ? mCountry.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPostalCode != null ? mPostalCode.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mStateOrProvince != null ? mStateOrProvince.hashCode() : 0);
        return result;
    }

    public static final class Builder {
        private JSONObject mAddress = new JSONObject();

        public Builder(
                @NonNull String street,
                @NonNull String houseNumberOrName,
                @NonNull String city,
                @NonNull String country,
                @NonNull String postalCode
        ) {
            try {
                mAddress.put(KEY_STREET, street);
                mAddress.put(KEY_HOUSE_NUMBER_OR_NAME, houseNumberOrName);
                mAddress.put(KEY_CITY, city);
                mAddress.put(KEY_COUNTRY, country);
                mAddress.put(KEY_POSTAL_CODE, postalCode);
            } catch (JSONException e) {
                //this should never happen.
                throw new RuntimeException("Failed to create Address", e);
            }
        }

        @NonNull
        public Builder setStateOrProvince(@Nullable String stateOrProvince) {
            try {
                mAddress.put(KEY_STATE_OR_PROVINCE, stateOrProvince);
            } catch (JSONException e) {
                //this should never happen
                throw new RuntimeException("Failed to create Address", e);
            }
            return this;
        }

        @NonNull
        public Address build() {

            try {
                return JsonObject.parseFrom(mAddress, Address.class);
            } catch (JSONException e) {
                //This should never happen.
                throw new RuntimeException("Failed to create Address", e);
            }
        }
    }
}
