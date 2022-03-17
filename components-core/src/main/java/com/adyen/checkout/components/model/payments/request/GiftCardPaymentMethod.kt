/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
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

@SuppressWarnings({"AbbreviationAsWordInName", "MemberName", "PMD.DataClass"})
public class GiftCardPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<GiftCardPaymentMethod> CREATOR = new Creator<>(GiftCardPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.GIFTCARD;

    private static final String ENCRYPTED_CARD_NUMBER = "encryptedCardNumber";
    private static final String ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode";
    private static final String BRAND = "brand";

    @NonNull
    public static final Serializer<GiftCardPaymentMethod> SERIALIZER = new Serializer<GiftCardPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull GiftCardPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(ENCRYPTED_CARD_NUMBER, modelObject.getEncryptedCardNumber());
                jsonObject.putOpt(ENCRYPTED_SECURITY_CODE, modelObject.getEncryptedSecurityCode());
                jsonObject.putOpt(BRAND, modelObject.getBrand());
            } catch (JSONException e) {
                throw new ModelSerializationException(GooglePayPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public GiftCardPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final GiftCardPaymentMethod giftcardPaymentMethod = new GiftCardPaymentMethod();

            // getting parameters from parent class
            giftcardPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            giftcardPaymentMethod.setEncryptedCardNumber(jsonObject.optString(ENCRYPTED_CARD_NUMBER, null));
            giftcardPaymentMethod.setEncryptedSecurityCode(jsonObject.optString(ENCRYPTED_SECURITY_CODE, null));
            giftcardPaymentMethod.setBrand(jsonObject.optString(BRAND));

            return giftcardPaymentMethod;
        }
    };

    private String encryptedCardNumber;
    private String encryptedSecurityCode;
    private String brand;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getEncryptedCardNumber() {
        return encryptedCardNumber;
    }

    public void setEncryptedCardNumber(@Nullable String encryptedCardNumber) {
        this.encryptedCardNumber = encryptedCardNumber;
    }

    @Nullable
    public String getEncryptedSecurityCode() {
        return encryptedSecurityCode;
    }

    public void setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
        this.encryptedSecurityCode = encryptedSecurityCode;
    }

    @Nullable
    public String getBrand() {
        return brand;
    }

    public void setBrand(@Nullable String brand) {
        this.brand = brand;
    }
}
