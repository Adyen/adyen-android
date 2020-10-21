/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/5/2019.
 */

package com.adyen.checkout.base;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class ActionComponentData extends ModelObject {

    @NonNull
    public static final Creator<ActionComponentData> CREATOR = new Creator<>(ActionComponentData.class);

    private static final String PAYMENT_DATA = "paymentData";
    private static final String DETAILS = "details";

    @NonNull
    public static final ModelObject.Serializer<ActionComponentData> SERIALIZER = new ModelObject.Serializer<ActionComponentData>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull ActionComponentData modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(PAYMENT_DATA, modelObject.getPaymentData());
                jsonObject.putOpt(DETAILS, modelObject.getDetails());
            } catch (JSONException e) {
                throw new ModelSerializationException(ActionComponentData.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public ActionComponentData deserialize(@NonNull JSONObject jsonObject) {
            final ActionComponentData actionComponentData = new ActionComponentData();
            actionComponentData.setPaymentData(jsonObject.optString(PAYMENT_DATA));
            actionComponentData.setDetails(jsonObject.optJSONObject(DETAILS));
            return actionComponentData;
        }
    };

    private String paymentData;
    private JSONObject details;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getPaymentData() {
        return paymentData;
    }

    public void setPaymentData(@Nullable String paymentData) {
        this.paymentData = paymentData;
    }

    @Nullable
    public JSONObject getDetails() {
        return details;
    }

    public void setDetails(@Nullable JSONObject details) {
        this.details = details;
    }
}
