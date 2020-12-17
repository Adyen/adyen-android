/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.components.model.payments.request;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class SepaPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<SepaPaymentMethod> CREATOR = new Creator<>(SepaPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.SEPA;

    private static final String OWNER_NAME = "ownerName";
    private static final String IBAN = "iban";

    @NonNull
    public static final Serializer<SepaPaymentMethod> SERIALIZER = new Serializer<SepaPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull SepaPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(OWNER_NAME, modelObject.getOwnerName());
                jsonObject.putOpt(IBAN, modelObject.getIban());
            } catch (JSONException e) {
                throw new ModelSerializationException(SepaPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public SepaPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final SepaPaymentMethod sepaPaymentMethod = new SepaPaymentMethod();

            // getting parameters from parent class
            sepaPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            sepaPaymentMethod.setOwnerName(jsonObject.optString(OWNER_NAME, null));
            sepaPaymentMethod.setIban(jsonObject.optString(IBAN, null));

            return sepaPaymentMethod;
        }
    };

    private String ownerName;
    private String iban;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(@Nullable String ownerName) {
        this.ownerName = ownerName;
    }

    @Nullable
    public String getIban() {
        return iban;
    }

    public void setIban(@Nullable String iban) {
        this.iban = iban;
    }
}
