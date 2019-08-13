/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exeption.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class GooglePayPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<GooglePayPaymentMethod> CREATOR = new Creator<>(GooglePayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.GOOGLE_PAY;

    private static final String PAY_WITH_GOOGLE_TOKEN = "paywithgoogle.token";

    @NonNull
    public static final Serializer<GooglePayPaymentMethod> SERIALIZER = new Serializer<GooglePayPaymentMethod>() {
        @NonNull
        @Override
        public JSONObject serialize(@NonNull GooglePayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(PAY_WITH_GOOGLE_TOKEN, modelObject.getToken());
            } catch (JSONException e) {
                throw new ModelSerializationException(GooglePayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public GooglePayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final GooglePayPaymentMethod googlePayPaymentMethod = new GooglePayPaymentMethod();

            // getting parameters from parent class
            googlePayPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            googlePayPaymentMethod.setToken(jsonObject.optString(PAY_WITH_GOOGLE_TOKEN, null));

            return googlePayPaymentMethod;
        }
    };

    private String token;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getToken() {
        return token;
    }

    public void setToken(@Nullable String token) {
        this.token = token;
    }
}
