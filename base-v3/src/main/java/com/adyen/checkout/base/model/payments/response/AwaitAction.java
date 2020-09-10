/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 18/8/2020.
 */

package com.adyen.checkout.base.model.payments.response;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.util.ActionTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class AwaitAction extends Action {
    @NonNull
    public static final Creator<AwaitAction> CREATOR = new Creator<>(AwaitAction.class);

    public static final String ACTION_TYPE = ActionTypes.AWAIT;

    @NonNull
    public static final Serializer<AwaitAction> SERIALIZER = new Serializer<AwaitAction>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull AwaitAction modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // Get parameters from parent class
                jsonObject.putOpt(Action.TYPE, modelObject.getType());
                jsonObject.putOpt(Action.PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(Action.PAYMENT_METHOD_TYPE, modelObject.getPaymentMethodType());
            } catch (JSONException e) {
                throw new ModelSerializationException(RedirectAction.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public AwaitAction deserialize(@NonNull JSONObject jsonObject) {
            final AwaitAction awaitAction = new AwaitAction();

            // getting parameters from parent class
            awaitAction.setType(jsonObject.optString(Action.TYPE, null));
            awaitAction.setPaymentData(jsonObject.optString(Action.PAYMENT_DATA, null));
            awaitAction.setPaymentMethodType(jsonObject.optString(Action.PAYMENT_METHOD_TYPE, null));
            return awaitAction;
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }
}
