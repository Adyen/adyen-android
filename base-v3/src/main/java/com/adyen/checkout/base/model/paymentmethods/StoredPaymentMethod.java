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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class StoredPaymentMethod extends PaymentMethod {
    @NonNull
    public static final Creator<StoredPaymentMethod> CREATOR = new Creator<>(StoredPaymentMethod.class);

    private static final String BRAND = "brand";
    private static final String EXPIRY_MONTH = "expiryMonth";
    private static final String EXPIRY_YEAR = "expiryYear";
    private static final String HOLDER_NAME = "holderName";
    private static final String ID = "id";
    private static final String LAST_FOUR = "lastFour";
    private static final String SHOPPER_EMAIL = "shopperEmail";
    private static final String SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions";

    private static final String ECOMMERCE = "Ecommerce";

    @NonNull
    public static final Serializer<StoredPaymentMethod> SERIALIZER = new Serializer<StoredPaymentMethod>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull StoredPaymentMethod modelObject) {
            // Get parameters from parent class
            final JSONObject jsonObject = PaymentMethod.SERIALIZER.serialize(modelObject);
            try {
                jsonObject.putOpt(BRAND, modelObject.getBrand());
                jsonObject.putOpt(EXPIRY_MONTH, modelObject.getExpiryMonth());
                jsonObject.putOpt(EXPIRY_YEAR, modelObject.getExpiryYear());
                jsonObject.putOpt(HOLDER_NAME, modelObject.getHolderName());
                jsonObject.putOpt(ID, modelObject.getId());
                jsonObject.putOpt(LAST_FOUR, modelObject.getLastFour());
                jsonObject.putOpt(SHOPPER_EMAIL, modelObject.getShopperEmail());
                jsonObject.putOpt(SUPPORTED_SHOPPER_INTERACTIONS, new JSONArray(modelObject.getSupportedShopperInteractions()));

            } catch (JSONException e) {
                throw new ModelSerializationException(StoredPaymentMethod.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public StoredPaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final StoredPaymentMethod storedPaymentMethod = new StoredPaymentMethod();

            // getting parameters from parent class
            final PaymentMethod paymentMethod = PaymentMethod.SERIALIZER.deserialize(jsonObject);
            storedPaymentMethod.setConfiguration(paymentMethod.getConfiguration());
            storedPaymentMethod.setDetails(paymentMethod.getDetails());
            storedPaymentMethod.setGroup(paymentMethod.getGroup());
            storedPaymentMethod.setName(paymentMethod.getName());
            storedPaymentMethod.setPaymentMethodData(paymentMethod.getPaymentMethodData());
            storedPaymentMethod.setSupportsRecurring(paymentMethod.getSupportsRecurring());
            storedPaymentMethod.setType(paymentMethod.getType());

            storedPaymentMethod.setBrand(jsonObject.optString(BRAND));
            storedPaymentMethod.setExpiryMonth(jsonObject.optString(EXPIRY_MONTH));
            storedPaymentMethod.setExpiryYear(jsonObject.optString(EXPIRY_YEAR));
            storedPaymentMethod.setHolderName(jsonObject.optString(HOLDER_NAME));
            storedPaymentMethod.setId(jsonObject.optString(ID));
            storedPaymentMethod.setLastFour(jsonObject.optString(LAST_FOUR));
            storedPaymentMethod.setShopperEmail(jsonObject.optString(SHOPPER_EMAIL));

            final List<String> supportedShopperInteractions = JsonUtils.parseOptStringList(jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS));

            if (supportedShopperInteractions != null) {
                storedPaymentMethod.setSupportedShopperInteractions(supportedShopperInteractions);
            }

            return storedPaymentMethod;
        }
    };

    private String brand;
    private String expiryMonth;
    private String expiryYear;
    private String holderName;
    private String id;
    private String lastFour;
    private String shopperEmail;
    private List<String> supportedShopperInteractions = Collections.emptyList();

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @NonNull
    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(@Nullable String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    @NonNull
    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(@Nullable String expiryYear) {
        this.expiryYear = expiryYear;
    }

    @NonNull
    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(@Nullable String lastFour) {
        this.lastFour = lastFour;
    }

    @NonNull
    public String getBrand() {
        return brand;
    }

    public void setBrand(@NonNull String brand) {
        this.brand = brand;
    }

    @NonNull
    public List<String> getSupportedShopperInteractions() {
        return supportedShopperInteractions;
    }

    public void setSupportedShopperInteractions(@NonNull List<String> supportedShopperInteractions) {
        this.supportedShopperInteractions = supportedShopperInteractions;
    }

    @Nullable
    public String getHolderName() {
        return holderName;
    }

    public void setHolderName(@Nullable String holderName) {
        this.holderName = holderName;
    }

    @Nullable
    public String getShopperEmail() {
        return shopperEmail;
    }

    public void setShopperEmail(@Nullable String shopperEmail) {
        this.shopperEmail = shopperEmail;
    }

    public boolean isEcommerce() {
        return supportedShopperInteractions.contains(ECOMMERCE);
    }
}
