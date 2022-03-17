/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 29/5/2019.
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
    private static final String ENCRYPTED_PASSWORD = "encryptedPassword";
    private static final String TAX_NUMBER = "taxNumber";
    private static final String BRAND = "brand";
    private static final String THREEDS2_SDK_VERSION = "threeDS2SdkVersion";
    private static final String FUNDING_SOURCE = "fundingSource";

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
                jsonObject.putOpt(ENCRYPTED_PASSWORD, modelObject.getEncryptedPassword());
                jsonObject.putOpt(TAX_NUMBER, modelObject.getTaxNumber());
                jsonObject.putOpt(BRAND, modelObject.getBrand());
                jsonObject.putOpt(THREEDS2_SDK_VERSION, modelObject.getThreeDS2SdkVersion());
                jsonObject.putOpt(FUNDING_SOURCE, modelObject.getFundingSource());
            } catch (JSONException e) {
                throw new ModelSerializationException(CardPaymentMethod.class, e);
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
            cardPaymentMethod.setEncryptedPassword(jsonObject.optString(ENCRYPTED_PASSWORD, null));
            cardPaymentMethod.setTaxNumber(jsonObject.optString(TAX_NUMBER));
            cardPaymentMethod.setBrand(jsonObject.optString(BRAND));
            cardPaymentMethod.setThreeDS2SdkVersion(jsonObject.optString(THREEDS2_SDK_VERSION, null));
            cardPaymentMethod.setFundingSource(jsonObject.optString(FUNDING_SOURCE, null));

            return cardPaymentMethod;
        }
    };

    private String encryptedCardNumber;
    private String encryptedExpiryMonth;
    private String encryptedExpiryYear;
    private String encryptedSecurityCode;
    private String encryptedPassword;
    private String holderName;
    private String storedPaymentMethodId;
    private String taxNumber;
    private String brand;
    private String threeDS2SdkVersion;
    private String fundingSource;

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
    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public void setEncryptedPassword(@Nullable String encryptedPassword) {
        this.encryptedPassword = encryptedPassword;
    }

    @Nullable
    public String getTaxNumber() {
        return taxNumber;
    }

    public void setTaxNumber(@Nullable String taxNumber) {
        this.taxNumber = taxNumber;
    }

    @Nullable
    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(@Nullable String holderName) {
        this.holderName = holderName;
    }

    @Nullable
    public String getBrand() {
        return brand;
    }

    public void setBrand(@Nullable String brand) {
        this.brand = brand;
    }

    public void setStoredPaymentMethodId(@Nullable String storedPaymentMethodId) {
        this.storedPaymentMethodId = storedPaymentMethodId;
    }

    @Nullable
    public String getStoredPaymentMethodId() {
        return storedPaymentMethodId;
    }

    @Nullable
    public String getThreeDS2SdkVersion() {
        return threeDS2SdkVersion;
    }

    public void setThreeDS2SdkVersion(@Nullable String threeDS2SdkVersion) {
        this.threeDS2SdkVersion = threeDS2SdkVersion;
    }

    @Nullable
    public String getFundingSource() {
        return fundingSource;
    }

    public void setFundingSource(@Nullable String fundingSource) {
        this.fundingSource = fundingSource;
    }
}
