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

import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class IsReadyToPayRequestModel extends ModelObject {

    @NonNull
    public static final Creator<IsReadyToPayRequestModel> CREATOR = new Creator<>(IsReadyToPayRequestModel.class);

    private static final String API_VERSION = "apiVersion";
    private static final String API_VERSION_MINOR = "apiVersionMinor";
    private static final String ALLOWED_PAYMENT_METHODS = "allowedPaymentMethods";
    private static final String EXISTING_PAYMENT_METHOD_REQUIRED = "existingPaymentMethodRequired";

    @NonNull
    public static final Serializer<IsReadyToPayRequestModel> SERIALIZER = new Serializer<IsReadyToPayRequestModel>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull IsReadyToPayRequestModel modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(API_VERSION, modelObject.getApiVersion());
                jsonObject.putOpt(API_VERSION_MINOR, modelObject.getApiVersionMinor());
                jsonObject.putOpt(ALLOWED_PAYMENT_METHODS,
                        ModelUtils.serializeOptList(modelObject.getAllowedPaymentMethods(), GooglePayPaymentMethodModel.SERIALIZER));
                jsonObject.putOpt(EXISTING_PAYMENT_METHOD_REQUIRED, modelObject.isExistingPaymentMethodRequired());
            } catch (JSONException e) {
                throw new ModelSerializationException(IsReadyToPayRequestModel.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public IsReadyToPayRequestModel deserialize(@NonNull JSONObject jsonObject) {
            final IsReadyToPayRequestModel isReadyToPayRequestModel = new IsReadyToPayRequestModel();
            isReadyToPayRequestModel.setApiVersion(jsonObject.optInt(API_VERSION));
            isReadyToPayRequestModel.setApiVersionMinor(jsonObject.optInt(API_VERSION_MINOR));
            isReadyToPayRequestModel.setAllowedPaymentMethods(
                    ModelUtils.deserializeOptList(jsonObject.optJSONArray(ALLOWED_PAYMENT_METHODS), GooglePayPaymentMethodModel.SERIALIZER));
            isReadyToPayRequestModel.setExistingPaymentMethodRequired(jsonObject.optBoolean(EXISTING_PAYMENT_METHOD_REQUIRED));
            return isReadyToPayRequestModel;
        }
    };

    private int apiVersion;
    private int apiVersionMinor;
    private List<GooglePayPaymentMethodModel> allowedPaymentMethods;
    private boolean existingPaymentMethodRequired;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    public int getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(int apiVersion) {
        this.apiVersion = apiVersion;
    }

    public int getApiVersionMinor() {
        return apiVersionMinor;
    }

    public void setApiVersionMinor(int apiVersionMinor) {
        this.apiVersionMinor = apiVersionMinor;
    }

    @Nullable
    public List<GooglePayPaymentMethodModel> getAllowedPaymentMethods() {
        return allowedPaymentMethods;
    }

    public void setAllowedPaymentMethods(@Nullable List<GooglePayPaymentMethodModel> allowedPaymentMethods) {
        this.allowedPaymentMethods = allowedPaymentMethods;
    }

    public boolean isExistingPaymentMethodRequired() {
        return existingPaymentMethodRequired;
    }

    public void setExistingPaymentMethodRequired(boolean existingPaymentMethodRequired) {
        this.existingPaymentMethodRequired = existingPaymentMethodRequired;
    }
}
