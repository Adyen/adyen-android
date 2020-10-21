/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/5/2019.
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
public final class CardPaymentMethod extends PaymentMethodDetails {

    @NonNull
    public static final Creator<CardPaymentMethod> CREATOR = new Creator<>(CardPaymentMethod.class);

    public static final String PAYMENT_METHOD_TYPE = PaymentMethodTypes.SCHEME;

    private static final String ENCRYPTED_CARD_NUMBER = "encryptedCardNumber";
    private static final String ENCRYPTED_EXPIRY_MONTH = "encryptedExpiryMonth";
    private static final String ENCRYPTED_EXPIRY_YEAR = "encryptedExpiryYear";
    private static final String ENCRYPTED_SECURITY_CODE = "encryptedSecurityCode";
    private static final String HOLDER_NAME = "holderName";
    private static final String STORED_PAYMENT_METHOD_ID = "storedPaymentMethodId";

    @NonNull
    public static final Serializer<CardPaymentMethod> SERIALIZER = new Serializer<CardPaymentMethod>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull CardPaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                // getting parameters from parent class
                jsonObject.putOpt(PaymentMethodDetails.TYPE, modelObject.getType());

                jsonObject.putOpt(ENCRYPTED_CARD_NUMBER, modelObject.getEncryptedCardNumber());
                jsonObject.putOpt(ENCRYPTED_EXPIRY_MONTH, modelObject.getEncryptedExpiryMonth());
                jsonObject.putOpt(ENCRYPTED_EXPIRY_YEAR, modelObject.getEncryptedExpiryYear());
                jsonObject.putOpt(ENCRYPTED_SECURITY_CODE, modelObject.getEncryptedSecurityCode());
                jsonObject.putOpt(STORED_PAYMENT_METHOD_ID, modelObject.getStoredPaymentMethodId());
                jsonObject.putOpt(HOLDER_NAME, modelObject.getHolderName());
            } catch (JSONException e) {
                throw new ModelSerializationException(IdealPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public CardPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final CardPaymentMethod cardPaymentMethod = new CardPaymentMethod();

            // getting parameters from parent class
            cardPaymentMethod.setType(jsonObject.optString(PaymentMethodDetails.TYPE, null));

            cardPaymentMethod.setEncryptedCardNumber(jsonObject.optString(ENCRYPTED_CARD_NUMBER, null));
            cardPaymentMethod.setEncryptedExpiryMonth(jsonObject.optString(ENCRYPTED_EXPIRY_MONTH, null));
            cardPaymentMethod.setEncryptedExpiryYear(jsonObject.optString(ENCRYPTED_EXPIRY_YEAR, null));
            cardPaymentMethod.setStoredPaymentMethodId(jsonObject.optString(STORED_PAYMENT_METHOD_ID));
            cardPaymentMethod.setEncryptedSecurityCode(jsonObject.optString(ENCRYPTED_SECURITY_CODE, null));
            cardPaymentMethod.setHolderName(jsonObject.optString(HOLDER_NAME, null));

            return cardPaymentMethod;
        }
    };

    private String encryptedCardNumber;
    private String encryptedExpiryMonth;
    private String encryptedExpiryYear;
    private String encryptedSecurityCode;
    private String holderName;
    private String storedPaymentMethodId;

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
    public String getEncryptedExpiryMonth() {
        return encryptedExpiryMonth;
    }

    public void setEncryptedExpiryMonth(@Nullable String encryptedExpiryMonth) {
        this.encryptedExpiryMonth = encryptedExpiryMonth;
    }

    @Nullable
    public String getEncryptedExpiryYear() {
        return encryptedExpiryYear;
    }

    public void setEncryptedExpiryYear(@Nullable String encryptedExpiryYear) {
        this.encryptedExpiryYear = encryptedExpiryYear;
    }

    @Nullable
    public String getEncryptedSecurityCode() {
        return encryptedSecurityCode;
    }

    public void setEncryptedSecurityCode(@Nullable String encryptedSecurityCode) {
        this.encryptedSecurityCode = encryptedSecurityCode;
    }

    @Nullable
    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(@Nullable String holderName) {
        this.holderName = holderName;
    }

    /**
     * @deprecated use StoredPaymentMethodId instead
     */
    @Nullable
    @Deprecated
    public String getRecurringDetailReference() {
        return storedPaymentMethodId;
    }

    /**
     * @deprecated use StoredPaymentMethodId instead
     */
    @Deprecated
    public void setRecurringDetailReference(@Nullable String recurringDetailReference) {
        this.storedPaymentMethodId = recurringDetailReference;
    }

    public void setStoredPaymentMethodId(@Nullable String storedPaymentMethodId) {
        this.storedPaymentMethodId = storedPaymentMethodId;
    }

    @Nullable
    public String getStoredPaymentMethodId() {
        return this.storedPaymentMethodId;
    }
}
