/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class Threeds2FingerprintAction extends Action {
    @NonNull
    public static final Creator<Threeds2FingerprintAction> CREATOR = new Creator<>(Threeds2FingerprintAction.class);

    public static final String ACTION_TYPE = ActionTypes.THREEDS2_FINGERPRINT;

    private static final String TOKEN = "token";

    @NonNull
    public static final Serializer<Threeds2FingerprintAction> SERIALIZER = new Serializer<Threeds2FingerprintAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Threeds2FingerprintAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(TOKEN, modelObject.getToken());
            } catch (JSONException e) {
                throw new ModelSerializationException(Threeds2FingerprintAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public Threeds2FingerprintAction deserialize(@NonNull JSONObject jsonObject) {
            final Threeds2FingerprintAction threeds2FingerprintAction = new Threeds2FingerprintAction();

            // getting parameters from parent class
            threeds2FingerprintAction.setType(jsonObject.optString(Action.TYPE, null));
            threeds2FingerprintAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            threeds2FingerprintAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            threeds2FingerprintAction.setToken(jsonObject.optString(TOKEN));
            return threeds2FingerprintAction;
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
