/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/7/2019.
 */

package com.adyen.checkout.googlepay.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings("MemberName")
public class ShippingAddressParameters extends ModelObject {

    @NonNull
    public static final Creator<ShippingAddressParameters> CREATOR = new Creator<>(ShippingAddressParameters.class);

    private static final String ALLOWED_COUNTRY_CODES = "allowedCountryCodes";
    private static final String PHONE_NUMBER_REQUIRED = "phoneNumberRequired";

    @NonNull
    public static final Serializer<ShippingAddressParameters> SERIALIZER = new Serializer<ShippingAddressParameters>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull ShippingAddressParameters modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ALLOWED_COUNTRY_CODES, JsonUtils.serializeOptStringList(modelObject.getAllowedCountryCodes()));
                jsonObject.putOpt(ALLOWED_COUNTRY_CODES, modelObject.isPhoneNumberRequired());

            } catch (JSONException e) {
                throw new ModelSerializationException(ShippingAddressParameters.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public ShippingAddressParameters deserialize(@NonNull JSONObject jsonObject) {
            final ShippingAddressParameters shippingAddressParameters = new ShippingAddressParameters();
            shippingAddressParameters.setAllowedCountryCodes(JsonUtils.parseOptStringList(jsonObject.optJSONArray(PHONE_NUMBER_REQUIRED)));
            shippingAddressParameters.setPhoneNumberRequired(jsonObject.optBoolean(PHONE_NUMBER_REQUIRED));
            return shippingAddressParameters;
        }
    };

    private List<String> allowedCountryCodes;
    private boolean phoneNumberRequired;


    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public List<String> getAllowedCountryCodes() {
        return allowedCountryCodes;
    }

    public void setAllowedCountryCodes(@Nullable List<String> allowedCountryCodes) {
        this.allowedCountryCodes = allowedCountryCodes;
    }

    public boolean isPhoneNumberRequired() {
        return phoneNumberRequired;
    }

    public void setPhoneNumberRequired(boolean phoneNumberRequired) {
        this.phoneNumberRequired = phoneNumberRequired;
    }
}
