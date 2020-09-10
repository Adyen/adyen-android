/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/7/2019.
 */

package com.adyen.checkout.googlepay.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("MemberName")
public class PaymentMethodTokenizationSpecification extends ModelObject {

    @NonNull
    public static final Creator<PaymentMethodTokenizationSpecification> CREATOR = new Creator<>(PaymentMethodTokenizationSpecification.class);

    private static final String TYPE = "type";
    private static final String PARAMETERS = "parameters";

    @NonNull
    public static final Serializer<PaymentMethodTokenizationSpecification> SERIALIZER = new Serializer<PaymentMethodTokenizationSpecification>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull PaymentMethodTokenizationSpecification modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(PARAMETERS, ModelUtils.serializeOpt(modelObject.getParameters(), TokenizationParameters.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethodTokenizationSpecification.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public PaymentMethodTokenizationSpecification deserialize(@NonNull JSONObject jsonObject) {
            final PaymentMethodTokenizationSpecification paymentMethodTokenizationSpecification = new PaymentMethodTokenizationSpecification();
            paymentMethodTokenizationSpecification.setType(jsonObject.optString(TYPE, null));
            paymentMethodTokenizationSpecification.setParameters(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(PARAMETERS), TokenizationParameters.SERIALIZER));
            return paymentMethodTokenizationSpecification;
        }
    };

    private String type;
    private TokenizationParameters parameters;

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
    public TokenizationParameters getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable TokenizationParameters parameters) {
        this.parameters = parameters;
    }
}
