/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */

package com.adyen.checkout.base.model.paymentmethods;

import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONException;
import org.json.JSONObject;

// Suppressing these warnings for compatibility with GSON and Moshi.
@SuppressWarnings({"MemberName", "PMD.DataClass"})
public final class Bank extends ModelObject {
    @NonNull
    public static final Creator<Bank> CREATOR = new Creator<>(Bank.class);

    private static final String BANK_ACCOUNT_NUMBER = "bankAccountNumber";
    private static final String BANK_CITY = "bankCity";
    private static final String BANK_LOCATION_ID = "bankLocationId";
    private static final String BANK_NAME = "bankName";
    private static final String BIC = "bic";
    private static final String COUNTRY_CODE = "countryCode";
    private static final String IBAN = "iban";
    private static final String OWNER_NAME = "ownerName";
    private static final String TAX_ID = "taxId";

    @NonNull
    public static final Serializer<Bank> SERIALIZER = new Serializer<Bank>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Bank modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(BANK_ACCOUNT_NUMBER, modelObject.getBankAccountNumber());
                jsonObject.putOpt(BANK_CITY, modelObject.getBankCity());
                jsonObject.putOpt(BANK_LOCATION_ID, modelObject.getBankLocationId());
                jsonObject.putOpt(BANK_NAME, modelObject.getBankName());
                jsonObject.putOpt(BIC, modelObject.getBic());
                jsonObject.putOpt(COUNTRY_CODE, modelObject.getCountryCode());
                jsonObject.putOpt(IBAN, modelObject.getIban());
                jsonObject.putOpt(OWNER_NAME, modelObject.getOwnerName());
                jsonObject.putOpt(TAX_ID, modelObject.getTaxId());

            } catch (JSONException e) {
                throw new ModelSerializationException(Bank.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Bank deserialize(@NonNull JSONObject jsonObject) {
            final Bank bank = new Bank();
            bank.setBankAccountNumber(jsonObject.optString(BANK_ACCOUNT_NUMBER, null));
            bank.setBankCity(jsonObject.optString(BANK_CITY, null));
            bank.setBankLocationId(jsonObject.optString(BANK_LOCATION_ID, null));
            bank.setBankName(jsonObject.optString(BANK_NAME, null));
            bank.setBic(jsonObject.optString(BIC, null));
            bank.setCountryCode(jsonObject.optString(COUNTRY_CODE, null));
            bank.setIban(jsonObject.optString(IBAN, null));
            bank.setOwnerName(jsonObject.optString(OWNER_NAME, null));
            bank.setTaxId(jsonObject.optString(TAX_ID, null));
            return bank;
        }
    };

    private String bankAccountNumber;
    private String bankCity;
    private String bankLocationId;
    private String bankName;
    private String bic;
    private String mCountryCode;
    private String iban;
    private String ownerName;
    private String taxId;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }

    @Nullable
    public String getBankCity() {
        return bankCity;
    }

    @Nullable
    public String getBankLocationId() {
        return bankLocationId;
    }

    @Nullable
    public String getBankName() {
        return bankName;
    }

    @Nullable
    public String getBic() {
        return bic;
    }

    @Nullable
    public String getCountryCode() {
        return mCountryCode;
    }

    @Nullable
    public String getIban() {
        return iban;
    }

    @Nullable
    public String getOwnerName() {
        return ownerName;
    }

    @Nullable
    public String getTaxId() {
        return taxId;
    }

    public void setBankAccountNumber(@Nullable String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }

    public void setBankCity(@Nullable String bankCity) {
        this.bankCity = bankCity;
    }

    public void setBankLocationId(@Nullable String bankLocationId) {
        this.bankLocationId = bankLocationId;
    }

    public void setBankName(@Nullable String bankName) {
        this.bankName = bankName;
    }

    public void setBic(@Nullable String bic) {
        this.bic = bic;
    }

    public void setCountryCode(@Nullable String countryCode) {
        mCountryCode = countryCode;
    }

    public void setIban(@Nullable String iban) {
        this.iban = iban;
    }

    public void setOwnerName(@Nullable String ownerName) {
        this.ownerName = ownerName;
    }

    public void setTaxId(@Nullable String taxId) {
        this.taxId = taxId;
    }


}
