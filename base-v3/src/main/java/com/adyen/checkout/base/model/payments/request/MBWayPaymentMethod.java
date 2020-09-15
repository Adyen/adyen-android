/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
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

@SuppressWarnings({"AbbreviationAsWordInName", "MemberName", "PMD.DataClass"})
public class MBWayPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<MBWayPaymentMethod> CREATOR = new Creator<>(MBWayPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.MB_WAY;

    private static final String SHOPPER_EMAIL = "shopperEmail";
    private static final String TELEPHONE_NUMBER = "telephoneNumber";

    @NonNull
    public static final Serializer<MBWayPaymentMethod> SERIALIZER = new Serializer<MBWayPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull MBWayPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(SHOPPER_EMAIL, modelObject.getShopperEmail());
                jsonObject.putOpt(TELEPHONE_NUMBER, modelObject.getTelephoneNumber());
            } catch (JSONException e) {
                throw new ModelSerializationException(GooglePayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public MBWayPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final MBWayPaymentMethod mbWayPaymentMethod = new MBWayPaymentMethod();

            // getting parameters from parent class
            mbWayPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            mbWayPaymentMethod.setShopperEmail(jsonObject.optString(SHOPPER_EMAIL, null));
            mbWayPaymentMethod.setTelephoneNumber(jsonObject.optString(TELEPHONE_NUMBER, null));

            return mbWayPaymentMethod;
        }
    };

    private String shopperEmail;
    private String telephoneNumber;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getShopperEmail() {
        return shopperEmail;
    }

    public void setShopperEmail(@Nullable String shopperEmail) {
        this.shopperEmail = shopperEmail;
    }

    @Nullable
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(@Nullable String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }
}
