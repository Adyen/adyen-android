/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */

package com.adyen.checkout.components.model.paymentmethods;

import android.os.Parcel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;
import com.adyen.checkout.core.model.ModelObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class StoredPaymentMethod extends ModelObject {
    @NonNull
    public static final Creator<StoredPaymentMethod> CREATOR = new Creator<>(StoredPaymentMethod.class);

    private static final String TYPE = "type";
    private static final String NAME = "name";
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
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(NAME, modelObject.getName());
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

            storedPaymentMethod.setType(jsonObject.optString(TYPE));
            storedPaymentMethod.setName(jsonObject.optString(NAME));
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

    private String type;
    private String name;
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
    public String getType() {
        return type;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getBrand() {
        return brand;
    }

    @Nullable
    public String getExpiryMonth() {
        return expiryMonth;
    }

    @Nullable
    public String getExpiryYear() {
        return expiryYear;
    }

    @Nullable
    public String getHolderName() {
        return holderName;
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getLastFour() {
        return lastFour;
    }

    @Nullable
    public String getShopperEmail() {
        return shopperEmail;
    }

    @Nullable
    public List<String> getSupportedShopperInteractions() {
        return supportedShopperInteractions;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setBrand(@Nullable String brand) {
        this.brand = brand;
    }

    public void setExpiryMonth(@Nullable String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    public void setExpiryYear(@Nullable String expiryYear) {
        this.expiryYear = expiryYear;
    }

    public void setHolderName(@Nullable String holderName) {
        this.holderName = holderName;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setLastFour(@Nullable String lastFour) {
        this.lastFour = lastFour;
    }

    public void setShopperEmail(@Nullable String shopperEmail) {
        this.shopperEmail = shopperEmail;
    }

    public void setSupportedShopperInteractions(@Nullable List<String> supportedShopperInteractions) {
        this.supportedShopperInteractions = supportedShopperInteractions;
    }

    public boolean isEcommerce() {
        return supportedShopperInteractions.contains(ECOMMERCE);
    }
}
