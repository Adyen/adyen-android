/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exeption.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class Threeds2ChallengeAction extends Action {
    @NonNull
    public static final Creator<Threeds2ChallengeAction> CREATOR = new Creator<>(Threeds2ChallengeAction.class);

    public static final String ACTION_TYPE = "threeDS2Challenge";

    private static final String TOKEN = "token";

    @NonNull
    public static final Serializer<Threeds2ChallengeAction> SERIALIZER = new Serializer<Threeds2ChallengeAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Threeds2ChallengeAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());

                jsonObject.putOpt(TOKEN, modelObject.getToken());
            } catch (JSONException e) {
                throw new ModelSerializationException(Threeds2ChallengeAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public Threeds2ChallengeAction deserialize(@NonNull JSONObject jsonObject) {
            final Threeds2ChallengeAction threeds2ChallengeAction = new Threeds2ChallengeAction();

            // getting parameters from parent class
            threeds2ChallengeAction.setType(jsonObject.optString(Action.TYPE, null));
            threeds2ChallengeAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));

            threeds2ChallengeAction.setToken(jsonObject.optString(TOKEN, null));
            return threeds2ChallengeAction;
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
