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

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class StatusResponse extends ModelObject {
    @NonNull
    public static final Creator<StatusResponse> CREATOR = new Creator<>(StatusResponse.class);

    public static final String TYPE = "type";
    public static final String PAYLOAD = "payload";
    public static final String RESULT_CODE = "resultCode";

    @NonNull
    public static final Serializer<StatusResponse> SERIALIZER = new Serializer<StatusResponse>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull StatusResponse modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(PAYLOAD, modelObject.getPayload());
                jsonObject.putOpt(RESULT_CODE, modelObject.getResultCode());
            } catch (JSONException e) {
                throw new ModelSerializationException(Address.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public StatusResponse deserialize(@NonNull JSONObject jsonObject) {
            final StatusResponse statusResponse = new StatusResponse();

            statusResponse.setType(jsonObject.optString(TYPE, null));
            statusResponse.setPayload(jsonObject.optString(PAYLOAD, null));
            statusResponse.setResultCode(jsonObject.optString(RESULT_CODE, null));

            return statusResponse;
        }
    };

    private String type;
    private String payload;
    private String resultCode;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    public void setPayload(@Nullable String payload) {
        this.payload = payload;
    }

    @Nullable
    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(@Nullable String resultCode) {
        this.resultCode = resultCode;
    }

}
