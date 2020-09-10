/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/6/2019.
 */

package com.adyen.checkout.base.model.payments;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class Amount extends ModelObject {

    @NonNull
    public static final Creator<Amount> CREATOR = new Creator<>(Amount.class);

    @NonNull
    public static final Amount EMPTY;

    private static final String EMPTY_CURRENCY = "NONE";
    private static final int EMPTY_VALUE = -1;

    static {
        EMPTY = new Amount();
        EMPTY.setCurrency(EMPTY_CURRENCY);
        EMPTY.setValue(EMPTY_VALUE);
    }

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
            amount.setCurrency(jsonObject.optString(CURRENCY, null));
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

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public boolean isEmpty() {
        return EMPTY_CURRENCY.equals(currency) || value == EMPTY_VALUE;
    }
}
