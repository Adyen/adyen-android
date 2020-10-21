/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 28/5/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class IdealPaymentMethod extends IssuerListPaymentMethod {
    @NonNull
    public static final Creator<IdealPaymentMethod> CREATOR = new Creator<>(IdealPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = "ideal";

    @NonNull
    public static final Serializer<IdealPaymentMethod> SERIALIZER = new Serializer<IdealPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull IdealPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(IssuerListPaymentMethod.ISSUER, modelObject.getIssuer());


            } catch (JSONException e) {
                throw new ModelSerializationException(IdealPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public IdealPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final IdealPaymentMethod idealPaymentMethod = new IdealPaymentMethod();

            // getting parameters from parent class
            idealPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));
            idealPaymentMethod.setIssuer(jsonObject.optString(IssuerListPaymentMethod.ISSUER, null));

            return idealPaymentMethod;
        }
    };

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

}
