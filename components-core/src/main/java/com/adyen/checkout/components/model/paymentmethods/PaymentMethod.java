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
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class PaymentMethod extends ModelObject {
    @NonNull
    public static final Creator<PaymentMethod> CREATOR = new Creator<>(PaymentMethod.class);

    private static final String TYPE = "type";
    private static final String NAME = "name";
    // Brands is only used for type = scheme
    private static final String BRANDS = "brands";
    // Brand is only used for type = giftcard
    private static final String BRAND = "brand";
    private static final String FUNDING_SOURCE = "fundingSource";
    private static final String ISSUERS = "issuers";
    private static final String CONFIGURATION = "configuration";
    // This field is returned in older API versions, only used to retrieve the issuers list
    private static final String DETAILS = "details";

    @NonNull
    public static final Serializer<PaymentMethod> SERIALIZER = new Serializer<PaymentMethod>() {

        @Override
        @NonNull
        public JSONObject serialize(@NonNull PaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(BRANDS, JsonUtils.serializeOptStringList(modelObject.getBrands()));
                jsonObject.putOpt(BRAND, modelObject.getBrand());
                jsonObject.putOpt(FUNDING_SOURCE, modelObject.getFundingSource());
                jsonObject.putOpt(ISSUERS,
                        ModelUtils.serializeOptList(modelObject.getIssuers(), Issuer.SERIALIZER)
                );
                jsonObject.putOpt(CONFIGURATION, ModelUtils.serializeOpt(modelObject.getConfiguration(), Configuration.SERIALIZER));
                jsonObject.putOpt(DETAILS, ModelUtils.serializeOptList(modelObject.getDetails(), InputDetail.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethod.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public PaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setType(jsonObject.optString(TYPE, null));
            paymentMethod.setName(jsonObject.optString(NAME, null));
            paymentMethod.setBrands(JsonUtils.parseOptStringList(jsonObject.optJSONArray(BRANDS)));
            paymentMethod.setBrand(jsonObject.optString(BRAND, null));
            paymentMethod.setFundingSource(jsonObject.optString(FUNDING_SOURCE, null));
            paymentMethod.setIssuers(
                    ModelUtils.deserializeOptList(jsonObject.optJSONArray(ISSUERS), Issuer.SERIALIZER)
            );
            paymentMethod.setConfiguration(
                    ModelUtils.deserializeOpt(jsonObject.optJSONObject(CONFIGURATION), Configuration.SERIALIZER));
            paymentMethod.setDetails(ModelUtils.deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER));
            return paymentMethod;
        }
    };

    private String type;
    private String name;
    private List<String> brands;
    private String brand;
    private String fundingSource;
    private List<Issuer> issuers;
    private Configuration configuration;
    private List<InputDetail> details;

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
    public List<String> getBrands() {
        return brands;
    }

    @Nullable
    public String getBrand() {
        return brand;
    }

    @Nullable
    public String getFundingSource() {
        return fundingSource;
    }

    @Nullable
    public List<Issuer> getIssuers() {
        return issuers;
    }

    @Nullable
    public Configuration getConfiguration() {
        return configuration;
    }

    @Nullable
    public List<InputDetail> getDetails() {
        return details;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setBrands(@Nullable List<String> brands) {
        this.brands = brands;
    }

    public void setBrand(@Nullable String brand) {
        this.brand = brand;
    }

    public void setFundingSource(@Nullable String fundingSource) {
        this.fundingSource = fundingSource;
    }

    public void setIssuers(@Nullable List<Issuer> issuers) {
        this.issuers = issuers;
    }

    public void setConfiguration(@Nullable Configuration configuration) {
        this.configuration = configuration;
    }

    public void setDetails(@Nullable List<InputDetail> details) {
        this.details = details;
    }
}
