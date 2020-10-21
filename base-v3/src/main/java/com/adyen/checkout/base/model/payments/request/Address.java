/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/12/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass", "DeclarationOrder", "PMD.FieldDeclarationsShouldBeAtStartOfClass"})
public class Address extends ModelObject {
    @NonNull
    public static final Creator<Address> CREATOR = new Creator<>(Address.class);

    private static final String CITY = "city";
    private static final String COUNTRY = "country";
    private static final String HOUSE_NUMBER_OR_NAME = "houseNumberOrName";
    private static final String POSTAL_CODE = "postalCode";
    private static final String STATE_OR_PROVINCE = "stateOrProvince";
    private static final String STREET = "street";

    @NonNull
    public static final Serializer<Address> SERIALIZER = new Serializer<Address>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Address modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CITY, modelObject.getCity());
                jsonObject.putOpt(COUNTRY, modelObject.getCountry());
                jsonObject.putOpt(HOUSE_NUMBER_OR_NAME, modelObject.getHouseNumberOrName());
                jsonObject.putOpt(POSTAL_CODE, modelObject.getPostalCode());
                jsonObject.putOpt(STATE_OR_PROVINCE, modelObject.getStateOrProvince());
                jsonObject.putOpt(STREET, modelObject.getStreet());
            } catch (JSONException e) {
                throw new ModelSerializationException(Address.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Address deserialize(@NonNull JSONObject jsonObject) {
            final Address address = new Address();

            address.setCity(jsonObject.optString(CITY, null));
            address.setCountry(jsonObject.optString(COUNTRY, null));
            address.setHouseNumberOrName(jsonObject.optString(HOUSE_NUMBER_OR_NAME, null));
            address.setPostalCode(jsonObject.optString(POSTAL_CODE, null));
            address.setStateOrProvince(jsonObject.optString(STATE_OR_PROVINCE, null));
            address.setStreet(jsonObject.optString(STREET, null));

            return address;
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    private String city;
    private String country;
    private String houseNumberOrName;
    private String postalCode;
    private String stateOrProvince;
    private String street;

    @NonNull
    public String getCity() {
        return city;
    }

    public void setCity(@NonNull String city) {
        this.city = city;
    }

    @NonNull
    public String getCountry() {
        return country;
    }

    public void setCountry(@NonNull String country) {
        this.country = country;
    }

    @NonNull
    public String getHouseNumberOrName() {
        return houseNumberOrName;
    }

    public void setHouseNumberOrName(@NonNull String houseNumberOrName) {
        this.houseNumberOrName = houseNumberOrName;
    }

    @NonNull
    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(@NonNull String postalCode) {
        this.postalCode = postalCode;
    }

    @NonNull
    public String getStateOrProvince() {
        return stateOrProvince;
    }

    public void setStateOrProvince(@NonNull String stateOrProvince) {
        this.stateOrProvince = stateOrProvince;
    }

    @NonNull
    public String getStreet() {
        return street;
    }

    public void setStreet(@NonNull String street) {
        this.street = street;
    }
}

