/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class BlikPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<BlikPaymentMethod> CREATOR = new Creator<>(BlikPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.BLIK;

    private static final String BLIK_CODE = "blikCode";
    private static final String STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId";

    @NonNull
    public static final Serializer<BlikPaymentMethod> SERIALIZER = new Serializer<BlikPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull BlikPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(BLIK_CODE, modelObject.getBlikCode());
                jsonObject.putOpt(STORED_PAYMENT_METHOD_ID, modelObject.getStoredPaymentMethodId());
            } catch (JSONException e) {
                throw new ModelSerializationException(BlikPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public BlikPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final BlikPaymentMethod blikPaymentMethod = new BlikPaymentMethod();

            // getting parameters from parent class
            blikPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            blikPaymentMethod.setBlikCode(jsonObject.optString(BLIK_CODE, null));
            blikPaymentMethod.setStoredPaymentMethodId(jsonObject.optString(STORED_PAYMENT_METHOD_ID, null));

            return blikPaymentMethod;
        }
    };

    private String blikCode;
    private String storedPaymentMethodId;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getBlikCode() {
        return blikCode;
    }

    @Nullable
    public String getStoredPaymentMethodId() {
        return storedPaymentMethodId;
    }

    public void setBlikCode(@Nullable String blikCode) {
        this.blikCode = blikCode;
    }

    public void setStoredPaymentMethodId(@Nullable String storedPaymentMethodId) {
        this.storedPaymentMethodId = storedPaymentMethodId;
    }
}
