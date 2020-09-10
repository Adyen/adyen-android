/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 5/6/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class GenericPaymentMethod extends PaymentMethodDetails {
    @NonNull
    public static final Creator<GenericPaymentMethod> CREATOR = new Creator<>(GenericPaymentMethod.class);

    @NonNull
    public static final Serializer<GenericPaymentMethod> SERIALIZER = new Serializer<GenericPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull GenericPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

            } catch (JSONException e) {
                throw new ModelSerializationException(GenericPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public GenericPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            return new GenericPaymentMethod(jsonObject.optString(PaymentMethodDetails.TYPE, null));
        }
    };

    public GenericPaymentMethod(@Nullable String paymentType) {
        setType(paymentType);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }
}
