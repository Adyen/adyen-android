/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.components.model.payments.request;

import android.os.Parcel;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

public final class DotpayPaymentMethod extends IssuerListPaymentMethod {
    @NonNull
    public static final Creator<DotpayPaymentMethod> CREATOR = new Creator<>(DotpayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.DOTPAY;

    @NonNull
    public static final Serializer<DotpayPaymentMethod> SERIALIZER = new Serializer<DotpayPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull DotpayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(IssuerListPaymentMethod.ISSUER, modelObject.getIssuer());


            } catch (JSONException e) {
                throw new ModelSerializationException(DotpayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public DotpayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final DotpayPaymentMethod idealPaymentMethod = new DotpayPaymentMethod();

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
