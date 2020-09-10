/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/2/2019.
 */

package com.adyen.checkout.base.model;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.model.paymentmethods.PaymentMethod;
import com.adyen.checkout.base.model.paymentmethods.PaymentMethodsGroup;
import com.adyen.checkout.base.model.paymentmethods.StoredPaymentMethod;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Object that parses and holds the response data from the paymentMethods/ endpoint.
 */
@SuppressWarnings("MemberName")
public final class PaymentMethodsApiResponse extends ModelObject {
    @NonNull
    public static final Creator<PaymentMethodsApiResponse> CREATOR = new Creator<>(PaymentMethodsApiResponse.class);

    private static final String GROUPS = "groups";
    private static final String STORED_PAYMENT_METHODS = "storedPaymentMethods";
    private static final String PAYMENT_METHODS = "paymentMethods";

    @NonNull
    public static final Serializer<PaymentMethodsApiResponse> SERIALIZER = new Serializer<PaymentMethodsApiResponse>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull PaymentMethodsApiResponse modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(GROUPS, ModelUtils.serializeOptList(modelObject.getGroups(), PaymentMethodsGroup.SERIALIZER));
                jsonObject.putOpt(STORED_PAYMENT_METHODS,
                        ModelUtils.serializeOptList(modelObject.getStoredPaymentMethods(), StoredPaymentMethod.SERIALIZER));
                jsonObject.putOpt(PAYMENT_METHODS, ModelUtils.serializeOptList(modelObject.getPaymentMethods(), PaymentMethod.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethodsApiResponse.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public PaymentMethodsApiResponse deserialize(@NonNull JSONObject jsonObject) {
            final PaymentMethodsApiResponse paymentMethodsApiResponse = new PaymentMethodsApiResponse();
            paymentMethodsApiResponse.setGroups(ModelUtils.deserializeOptList(jsonObject.optJSONArray(GROUPS), PaymentMethodsGroup.SERIALIZER));
            paymentMethodsApiResponse.setStoredPaymentMethods(
                    ModelUtils.deserializeOptList(jsonObject.optJSONArray(STORED_PAYMENT_METHODS), StoredPaymentMethod.SERIALIZER));
            paymentMethodsApiResponse.setPaymentMethods(
                    ModelUtils.deserializeOptList(jsonObject.optJSONArray(PAYMENT_METHODS), PaymentMethod.SERIALIZER));
            return paymentMethodsApiResponse;
        }
    };

    private List<PaymentMethodsGroup> groups;
    private List<StoredPaymentMethod> storedPaymentMethods;
    private List<PaymentMethod> paymentMethods;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public List<PaymentMethodsGroup> getGroups() {
        return groups;
    }

    @Nullable
    public List<StoredPaymentMethod> getStoredPaymentMethods() {
        return storedPaymentMethods;
    }

    @Nullable
    public List<PaymentMethod> getPaymentMethods() {
        return paymentMethods;
    }

    public void setGroups(@Nullable List<PaymentMethodsGroup> groups) {
        this.groups = groups;
    }

    public void setStoredPaymentMethods(@Nullable List<StoredPaymentMethod> storedPaymentMethods) {
        this.storedPaymentMethods = storedPaymentMethods;
    }

    public void setPaymentMethods(@Nullable List<PaymentMethod> paymentMethods) {
        this.paymentMethods = paymentMethods;
    }
}
