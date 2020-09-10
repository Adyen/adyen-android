/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/7/2019.
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

@SuppressWarnings("MemberName")
public class BillingAddressParameters extends ModelObject {

    @NonNull
    public static final Creator<BillingAddressParameters> CREATOR = new Creator<>(BillingAddressParameters.class);

    private static final String FORMAT = "format";
    private static final String PHONE_NUMBER_REQUIRED = "phoneNumberRequired";

    @NonNull
    public static final Serializer<BillingAddressParameters> SERIALIZER = new Serializer<BillingAddressParameters>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull BillingAddressParameters modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(FORMAT, modelObject.getFormat());
                jsonObject.putOpt(PHONE_NUMBER_REQUIRED, modelObject.isPhoneNumberRequired());
            } catch (JSONException e) {
                throw new ModelSerializationException(BillingAddressParameters.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public BillingAddressParameters deserialize(@NonNull JSONObject jsonObject) {
            final BillingAddressParameters billingAddressParameters = new BillingAddressParameters();
            billingAddressParameters.setFormat(jsonObject.optString(FORMAT, null));
            billingAddressParameters.setPhoneNumberRequired(jsonObject.optBoolean(PHONE_NUMBER_REQUIRED));
            return billingAddressParameters;
        }
    };

    private String format;
    private boolean phoneNumberRequired;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getFormat() {
        return format;
    }

    public void setFormat(@Nullable String format) {
        this.format = format;
    }

    public boolean isPhoneNumberRequired() {
        return phoneNumberRequired;
    }

    public void setPhoneNumberRequired(boolean phoneNumberRequired) {
        this.phoneNumberRequired = phoneNumberRequired;
    }
}
