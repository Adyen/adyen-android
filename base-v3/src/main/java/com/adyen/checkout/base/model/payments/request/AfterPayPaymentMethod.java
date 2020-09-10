/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/11/2019.
 */

package com.adyen.checkout.base.model.payments.request;

import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.util.PaymentMethodTypes;
import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class AfterPayPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<AfterPayPaymentMethod> CREATOR = new Creator<>(AfterPayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.AFTER_PAY;

    private static final String CONSENT_CHECKBOX = "consentCheckbox";

    @NonNull
    public static final Serializer<AfterPayPaymentMethod> SERIALIZER = new Serializer<AfterPayPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull AfterPayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());
                jsonObject.putOpt(CONSENT_CHECKBOX, modelObject.isConsentCheckbox());
            } catch (JSONException e) {
                throw new ModelSerializationException(AfterPayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public AfterPayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final AfterPayPaymentMethod afterPayPaymentMethod = new AfterPayPaymentMethod();

            // getting parameters from parent class
            afterPayPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));
            afterPayPaymentMethod.setConsentCheckbox(jsonObject.optBoolean(CONSENT_CHECKBOX));

            return afterPayPaymentMethod;
        }
    };

    private boolean consentCheckbox;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    public boolean isConsentCheckbox() {
        return consentCheckbox;
    }

    public void setConsentCheckbox(@NonNull boolean consentCheckbox) {
        this.consentCheckbox = consentCheckbox;
    }
}
