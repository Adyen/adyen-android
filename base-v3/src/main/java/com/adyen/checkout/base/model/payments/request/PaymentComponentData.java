/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 23/7/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exeption.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class PaymentComponentData<PaymentMethodDetailsT extends PaymentMethodDetails> extends ModelObject {

    @NonNull
    public static final Creator<PaymentComponentData> CREATOR = new Creator<>(PaymentComponentData.class);

    private static final String PAYMENT_METHOD = "paymentMethod";
    private static final String STORE_PAYMENT_METHOD = "storePaymentMethod";
    private static final String SHOPPER_REFERENCE = "shopperReference";

    @NonNull
    public static final Serializer<PaymentComponentData> SERIALIZER = new Serializer<PaymentComponentData>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull PaymentComponentData modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(PAYMENT_METHOD, ModelUtils.serializeOpt(modelObject.getPaymentMethod(), PaymentMethodDetails.SERIALIZER));
                jsonObject.putOpt(STORE_PAYMENT_METHOD, modelObject.isStorePaymentMethodEnable());
                jsonObject.putOpt(SHOPPER_REFERENCE, modelObject.getShopperReference());
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentComponentData.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public PaymentComponentData deserialize(@NonNull JSONObject jsonObject) {
            final PaymentComponentData paymentComponentData = new PaymentComponentData();
            //noinspection unchecked
            paymentComponentData.setPaymentMethod(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(PAYMENT_METHOD), PaymentMethodDetails.SERIALIZER));
            paymentComponentData.setStorePaymentMethod(jsonObject.optBoolean(STORE_PAYMENT_METHOD));
            paymentComponentData.setShopperReference(jsonObject.optString(SHOPPER_REFERENCE));
            return paymentComponentData;
        }
    };

    private PaymentMethodDetailsT paymentMethod;
    private boolean storePaymentMethod;
    private String shopperReference;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public PaymentMethodDetailsT getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(@Nullable PaymentMethodDetailsT paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public void setStorePaymentMethod(boolean status) {
        storePaymentMethod = status;
    }

    public boolean isStorePaymentMethodEnable() {
        return storePaymentMethod;
    }

    public void setShopperReference(@NonNull String shopperReference) {
        this.shopperReference = shopperReference;
    }

    @Nullable
    public String getShopperReference() {
        return shopperReference;
    }
}
