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
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class Amount extends ModelObject {

    @NonNull
    public static final Creator<Amount> CREATOR = new Creator<>(Amount.class);

    private static final String CURRENCY = "currency";
    private static final String VALUE = "value";

    @NonNull
    public static final Serializer<Amount> SERIALIZER = new Serializer<Amount>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Amount modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CURRENCY, modelObject.getCurrency());
                jsonObject.putOpt(VALUE, modelObject.getValue());
            } catch (JSONException e) {
                throw new ModelSerializationException(Amount.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public Amount deserialize(@NonNull JSONObject jsonObject) {
            final Amount amount = new Amount();
            amount.setCurrency(jsonObject.optString(CURRENCY));
            amount.setValue(jsonObject.optInt(VALUE, -1));
            return amount;
        }
    };

    private String currency;
    private int value;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getCurrency() {
        return currency;
    }

    public void setCurrency(@Nullable String currency) {
        this.currency = currency;
    }

    @Nullable
    public int getValue() {
        return value;
    }

    public void setValue(@Nullable int value) {
        this.value = value;
    }
}
