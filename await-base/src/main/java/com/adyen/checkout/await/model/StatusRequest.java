/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/8/2020.
 */

package com.adyen.checkout.await.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.model.payments.request.Address;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class StatusRequest extends ModelObject {
    @NonNull
    public static final Creator<StatusRequest> CREATOR = new Creator<>(StatusRequest.class);

    public static final String PAYMENT_DATA = "paymentData";

    @NonNull
    public static final Serializer<StatusRequest> SERIALIZER = new Serializer<StatusRequest>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull StatusRequest modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(PAYMENT_DATA, modelObject.getPaymentData());
            } catch (JSONException e) {
                throw new ModelSerializationException(Address.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public StatusRequest deserialize(@NonNull JSONObject jsonObject) {
            final StatusRequest statusRequest = new StatusRequest();
            statusRequest.setPaymentData(jsonObject.optString(PAYMENT_DATA, null));
            return statusRequest;
        }
    };

    private String paymentData;

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
}
