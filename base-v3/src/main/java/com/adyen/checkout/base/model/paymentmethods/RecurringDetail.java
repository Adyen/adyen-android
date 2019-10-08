/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/5/2019.
 */

package com.adyen.checkout.base.model.paymentmethods;

import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.core.exception.ModelSerializationException;
import com.adyen.checkout.core.model.JsonUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public final class RecurringDetail extends PaymentMethod {
    @NonNull
    public static final Creator<RecurringDetail> CREATOR = new Creator<>(RecurringDetail.class);

    private static final String RECURRING_DETAIL_REFERENCE = "id";
    private static final String EXPIRY_MONTH = "expiryMonth";
    private static final String EXPIRY_YEAR = "expiryYear";
    private static final String LAST_FOUR = "lastFour";
    private static final String BRAND = "brand";
    private static final String SUPPORTED_SHOPPER_INTERACTIONS = "supportedShopperInteractions";

    private static final String ECOMMERCE = "Ecommerce";

    @NonNull
    public static final Serializer<RecurringDetail> SERIALIZER = new Serializer<RecurringDetail>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull RecurringDetail modelObject) {
            // Get parameters from parent class
            final JSONObject jsonObject = PaymentMethod.SERIALIZER.serialize(modelObject);
            try {
                jsonObject.putOpt(RECURRING_DETAIL_REFERENCE, modelObject.getId());
                jsonObject.putOpt(EXPIRY_MONTH, modelObject.getExpiryMonth());
                jsonObject.putOpt(EXPIRY_YEAR, modelObject.getExpiryYear());
                jsonObject.putOpt(LAST_FOUR, modelObject.getLastFour());
                jsonObject.putOpt(BRAND, modelObject.getBrand());
                jsonObject.putOpt(SUPPORTED_SHOPPER_INTERACTIONS, new JSONArray(modelObject.getSupportedShopperInteractions()));
            } catch (JSONException e) {
                throw new ModelSerializationException(RecurringDetail.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public RecurringDetail deserialize(@NonNull JSONObject jsonObject) {
            final RecurringDetail recurringDetail = new RecurringDetail();

            // getting parameters from parent class
            final PaymentMethod paymentMethod = PaymentMethod.SERIALIZER.deserialize(jsonObject);
            recurringDetail.setConfiguration(paymentMethod.getConfiguration());
            recurringDetail.setDetails(paymentMethod.getDetails());
            recurringDetail.setGroup(paymentMethod.getGroup());
            recurringDetail.setName(paymentMethod.getName());
            recurringDetail.setPaymentMethodData(paymentMethod.getPaymentMethodData());
            recurringDetail.setSupportsRecurring(paymentMethod.getSupportsRecurring());
            recurringDetail.setType(paymentMethod.getType());

            recurringDetail.setId(jsonObject.optString(RECURRING_DETAIL_REFERENCE));
            recurringDetail.setExpiryMonth(jsonObject.optString(EXPIRY_MONTH));
            recurringDetail.setExpiryYear(jsonObject.optString(EXPIRY_YEAR));
            recurringDetail.setLastFour(jsonObject.optString(LAST_FOUR));
            recurringDetail.setBrand(jsonObject.optString(BRAND));

            final List<String> supportedShopperInteractions = JsonUtils.parseOptStringList(jsonObject.optJSONArray(SUPPORTED_SHOPPER_INTERACTIONS));

            if (supportedShopperInteractions != null) {
                recurringDetail.setSupportedShopperInteractions(supportedShopperInteractions);
            }

            return recurringDetail;
        }
    };

    private String id;
    private String expiryMonth;
    private String expiryYear;
    private String lastFour;
    private String brand;
    private List<String> supportedShopperInteractions = Collections.emptyList();

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    @NonNull
    public String getExpiryMonth() {
        return expiryMonth;
    }

    public void setExpiryMonth(@NonNull String expiryMonth) {
        this.expiryMonth = expiryMonth;
    }

    @NonNull
    public String getExpiryYear() {
        return expiryYear;
    }

    public void setExpiryYear(@NonNull String expiryYear) {
        this.expiryYear = expiryYear;
    }

    @NonNull
    public String getLastFour() {
        return lastFour;
    }

    public void setLastFour(@NonNull String lastFour) {
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

    public boolean isEcommerce() {
        return supportedShopperInteractions.contains(ECOMMERCE);
    }
}
