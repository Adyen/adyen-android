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
public class GooglePayPaymentMethodModel extends ModelObject {

    @NonNull
    public static final Creator<GooglePayPaymentMethodModel> CREATOR = new Creator<>(GooglePayPaymentMethodModel.class);

    private static final String TYPE = "type";
    private static final String PARAMETERS = "parameters";
    private static final String TOKENIZATION_SPECIFICATION = "tokenizationSpecification";

    @NonNull
    public static final Serializer<GooglePayPaymentMethodModel> SERIALIZER = new Serializer<GooglePayPaymentMethodModel>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull GooglePayPaymentMethodModel modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(PARAMETERS, ModelUtils.serializeOpt(modelObject.getParameters(), CardParameters.SERIALIZER));
                jsonObject.putOpt(TOKENIZATION_SPECIFICATION,
                        ModelUtils.serializeOpt(modelObject.getTokenizationSpecification(), PaymentMethodTokenizationSpecification.SERIALIZER));

            } catch (JSONException e) {
                throw new ModelSerializationException(GooglePayPaymentMethodModel.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public GooglePayPaymentMethodModel deserialize(@NonNull JSONObject jsonObject) {
            final GooglePayPaymentMethodModel googlePayPaymentMethodModel = new GooglePayPaymentMethodModel();
            googlePayPaymentMethodModel.setType(jsonObject.optString(TYPE, null));
            googlePayPaymentMethodModel.setParameters(ModelUtils.deserializeOpt(jsonObject.optJSONObject(PARAMETERS), CardParameters.SERIALIZER));
            googlePayPaymentMethodModel.setTokenizationSpecification(ModelUtils.deserializeOpt(jsonObject.optJSONObject(TOKENIZATION_SPECIFICATION),
                    PaymentMethodTokenizationSpecification.SERIALIZER));
            return googlePayPaymentMethodModel;
        }
    };

    private String type;
    private CardParameters parameters;
    private PaymentMethodTokenizationSpecification tokenizationSpecification;

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
    public CardParameters getParameters() {
        return parameters;
    }

    public void setParameters(@Nullable CardParameters parameters) {
        this.parameters = parameters;
    }

    @Nullable
    public PaymentMethodTokenizationSpecification getTokenizationSpecification() {
        return tokenizationSpecification;
    }

    public void setTokenizationSpecification(@Nullable PaymentMethodTokenizationSpecification tokenizationSpecification) {
        this.tokenizationSpecification = tokenizationSpecification;
    }
}
