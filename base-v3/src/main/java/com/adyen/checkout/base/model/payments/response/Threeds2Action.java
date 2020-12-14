/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/12/2020.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.exception.NoConstructorException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class Threeds2Action extends Action {
    @NonNull
    public static final Creator<Threeds2Action> CREATOR = new Creator<>(Threeds2Action.class);

    public static final String ACTION_TYPE = ActionTypes.THREEDS2;

    private static final String TOKEN = "token";
    private static final String SUBTYPE = "subtype";

    @NonNull
    public static final Serializer<Threeds2Action> SERIALIZER = new Serializer<Threeds2Action>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Threeds2Action modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());

                jsonObject.putOpt(TOKEN, modelObject.getToken());
                jsonObject.putOpt(SUBTYPE, modelObject.getSubtype());
            } catch (JSONException e) {
                throw new ModelSerializationException(Threeds2Action.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public Threeds2Action deserialize(@NonNull JSONObject jsonObject) {
            final Threeds2Action threeds2ChallengeAction = new Threeds2Action();

            // getting parameters from parent class
            threeds2ChallengeAction.setType(jsonObject.optString(Action.TYPE, null));
            threeds2ChallengeAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            threeds2ChallengeAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));

            threeds2ChallengeAction.setToken(jsonObject.optString(TOKEN, null));
            threeds2ChallengeAction.setSubtype(jsonObject.optString(SUBTYPE, null));
            return threeds2ChallengeAction;
        }
    };

    private String token;
    private String subtype;

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

    @Nullable
    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(@Nullable String subtype) {
        this.subtype = subtype;
    }

    @Nullable
    public SubType getSubtypeEnum() {
        return parseStringToSubType(subtype);
    }

    @Nullable
    private SubType parseStringToSubType(@Nullable String type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case SubTypes.FINGERPRINT:
                return SubType.FINGERPRINT;
            case SubTypes.CHALLENGE:
                return SubType.CHALLENGE;
            default:
                return null;
        }
    }

    private static final class SubTypes {

        public static final String FINGERPRINT = "fingerprint";
        public static final String CHALLENGE = "challenge";

        private SubTypes() {
            throw new NoConstructorException();
        }
    }

    public enum SubType {
        FINGERPRINT,
        CHALLENGE
    }
}
