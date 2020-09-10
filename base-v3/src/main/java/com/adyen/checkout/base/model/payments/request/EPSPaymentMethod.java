/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("AbbreviationAsWordInName")
public final class EPSPaymentMethod extends IssuerListPaymentMethod {
    @NonNull
    public static final Creator<EPSPaymentMethod> CREATOR = new Creator<>(EPSPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.EPS;

    @NonNull
    public static final Serializer<EPSPaymentMethod> SERIALIZER = new Serializer<EPSPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull EPSPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(IssuerListPaymentMethod.ISSUER, modelObject.getIssuer());


            } catch (JSONException e) {
                throw new ModelSerializationException(EPSPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public EPSPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final EPSPaymentMethod idealPaymentMethod = new EPSPaymentMethod();

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
