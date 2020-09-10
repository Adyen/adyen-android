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
import com.adyen.checkout.core.model.ModelUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public class PaymentMethod extends ModelObject {
    @NonNull
    public static final Creator<PaymentMethod> CREATOR = new Creator<>(PaymentMethod.class);

    private static final String CONFIGURATION = "configuration";
    private static final String DETAILS = "details";
    private static final String GROUP = "group";
    private static final String NAME = "name";
    private static final String BRANDS = "brands";
    private static final String PAYMENT_METHOD_DATA = "paymentMethodData";
    private static final String SUPPORTS_RECURRING = "supportsRecurring";
    private static final String TYPE = "type";

    @NonNull
    public static final Serializer<PaymentMethod> SERIALIZER = new Serializer<PaymentMethod>() {

        @Override
        @NonNull
        public JSONObject serialize(@NonNull PaymentMethod modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CONFIGURATION, modelObject.getConfiguration());
                jsonObject.putOpt(DETAILS, ModelUtils.serializeOptList(modelObject.getDetails(), InputDetail.SERIALIZER));
                jsonObject.putOpt(GROUP, ModelUtils.serializeOpt(modelObject.getGroup(), Group.SERIALIZER));
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(BRANDS, JsonUtils.serializeOptStringList(modelObject.getBrands()));
                jsonObject.putOpt(PAYMENT_METHOD_DATA, modelObject.getPaymentMethodData());
                jsonObject.putOpt(SUPPORTS_RECURRING, modelObject.getSupportsRecurring());
                jsonObject.putOpt(TYPE, modelObject.getType());
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethod.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public PaymentMethod deserialize(@NonNull JSONObject jsonObject) {
            final PaymentMethod paymentMethod = new PaymentMethod();
            paymentMethod.setConfiguration(jsonObject.optString(CONFIGURATION, null));
            paymentMethod.setDetails(ModelUtils.deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER));
            paymentMethod.setGroup(ModelUtils.deserializeOpt(jsonObject.optJSONObject(GROUP), Group.SERIALIZER));
            paymentMethod.setName(jsonObject.optString(NAME, null));
            paymentMethod.setBrands(JsonUtils.parseOptStringList(jsonObject.optJSONArray(BRANDS)));
            paymentMethod.setPaymentMethodData(jsonObject.optString(PAYMENT_METHOD_DATA, null));
            paymentMethod.setSupportsRecurring(jsonObject.optBoolean(SUPPORTS_RECURRING, false));
            paymentMethod.setType(jsonObject.optString(TYPE, null));
            return paymentMethod;
        }
    };

    // TODO: 15/04/2019 how to handle the configuration??
    // Configuration is a generic data object that can change per payment method. Save raw string to deserialize later as needed.
    private String configuration;
    private List<InputDetail> details;
    private Group group;
    private String name;
    private List<String> brands;
    private String paymentMethodData;
    private boolean supportsRecurring;
    private String type;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getConfiguration() {
        return configuration;
    }

    @Nullable
    public List<InputDetail> getDetails() {
        return details;
    }

    @Nullable
    public Group getGroup() {
        return group;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getPaymentMethodData() {
        return paymentMethodData;
    }

    public boolean getSupportsRecurring() {
        return supportsRecurring;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setConfiguration(@Nullable String configuration) {
        this.configuration = configuration;
    }

    public void setDetails(@Nullable List<InputDetail> details) {
        this.details = details;
    }

    public void setGroup(@Nullable Group group) {
        this.group = group;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setBrands(@Nullable List<String> brands) {
        this.brands = brands;
    }

    @Nullable
    public List<String> getBrands() {
        return brands;
    }

    public void setPaymentMethodData(@Nullable String paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public void setSupportsRecurring(boolean supportsRecurring) {
        this.supportsRecurring = supportsRecurring;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }
}
