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

public final class OpenBankingPaymentMethod extends IssuerListPaymentMethod {
    @NonNull
    public static final Creator<OpenBankingPaymentMethod> CREATOR = new Creator<>(OpenBankingPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.OPEN_BANKING;

    @NonNull
    public static final Serializer<OpenBankingPaymentMethod> SERIALIZER = new Serializer<OpenBankingPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull OpenBankingPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(IssuerListPaymentMethod.ISSUER, modelObject.getIssuer());


            } catch (JSONException e) {
                throw new ModelSerializationException(OpenBankingPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public OpenBankingPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final OpenBankingPaymentMethod idealPaymentMethod = new OpenBankingPaymentMethod();

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
