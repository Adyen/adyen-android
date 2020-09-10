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
public final class InputDetail extends ModelObject {
    @NonNull
    public static final Creator<InputDetail> CREATOR = new Creator<>(InputDetail.class);

    private static final String CONFIGURATION = "configuration";
    private static final String DETAILS = "details";
    private static final String ITEM_SEARCH_URL = "itemSearchUrl";
    private static final String ITEMS = "items";
    private static final String KEY = "key";
    private static final String NAME = "name";
    private static final String OPTIONAL = "optional";
    private static final String TYPE = "type";
    private static final String VALIDATION_TYPE = "validationType";
    private static final String VALUE = "value";

    @NonNull
    public static final Serializer<InputDetail> SERIALIZER = new Serializer<InputDetail>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull InputDetail modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(CONFIGURATION, modelObject.getConfiguration());
                jsonObject.putOpt(DETAILS, ModelUtils.serializeOptList(modelObject.getDetails(), InputDetail.SERIALIZER));
                jsonObject.putOpt(ITEM_SEARCH_URL, modelObject.getItemSearchUrl());
                jsonObject.putOpt(ITEMS, ModelUtils.serializeOptList(modelObject.getItems(), Item.SERIALIZER));
                jsonObject.putOpt(KEY, modelObject.getKey());
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(OPTIONAL, modelObject.isOptional());
                jsonObject.putOpt(TYPE, modelObject.getType());
                jsonObject.putOpt(VALIDATION_TYPE, modelObject.getValidationType());
                jsonObject.putOpt(VALUE, modelObject.getValue());
            } catch (JSONException e) {
                throw new ModelSerializationException(InputDetail.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public InputDetail deserialize(@NonNull JSONObject jsonObject) {
            final InputDetail inputDetail = new InputDetail();
            inputDetail.setConfiguration(jsonObject.optString(CONFIGURATION, null));
            inputDetail.setDetails(ModelUtils.deserializeOptList(jsonObject.optJSONArray(DETAILS), InputDetail.SERIALIZER));
            inputDetail.setItemSearchUrl(jsonObject.optString(ITEM_SEARCH_URL, null));
            inputDetail.setItems(ModelUtils.deserializeOptList(jsonObject.optJSONArray(ITEMS), Item.SERIALIZER));
            inputDetail.setKey(jsonObject.optString(KEY, null));
            inputDetail.setName(jsonObject.optString(NAME, null));
            inputDetail.setOptional(jsonObject.optBoolean(OPTIONAL));
            inputDetail.setType(jsonObject.optString(TYPE, null));
            inputDetail.setValidationType(jsonObject.optString(VALIDATION_TYPE, null));
            inputDetail.setValue(jsonObject.optString(VALUE, null));
            return inputDetail;
        }
    };

    // TODO: 15/04/2019 how to handle the configuration??
    // Configuration is a generic data object that can change per payment method. Save raw string to deserialize later as needed.
    private String configuration;
    private List<InputDetail> details;
    private String itemSearchUrl;
    private List<Item> items;
    private String key;
    private String name;
    private boolean optional;
    private String type;
    private String validationType;
    private String value;

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
    public String getItemSearchUrl() {
        return itemSearchUrl;
    }

    @Nullable
    public List<Item> getItems() {
        return items;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public boolean isOptional() {
        return optional;
    }

    @NonNull
    public String getType() {
        return type;
    }

    @Nullable
    public String getValidationType() {
        return validationType;
    }

    @Nullable
    public String getValue() {
        return value;
    }

    public void setConfiguration(@Nullable String configuration) {
        this.configuration = configuration;
    }

    public void setDetails(@Nullable List<InputDetail> details) {
        this.details = details;
    }

    public void setItemSearchUrl(@Nullable String itemSearchUrl) {
        this.itemSearchUrl = itemSearchUrl;
    }

    public void setItems(@Nullable List<Item> items) {
        this.items = items;
    }

    public void setKey(@Nullable String key) {
        this.key = key;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setOptional(boolean optional) {
        this.optional = optional;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public void setValidationType(@Nullable String validationType) {
        this.validationType = validationType;
    }

    public void setValue(@Nullable String value) {
        this.value = value;
    }
}
