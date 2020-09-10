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

import java.util.List;

@SuppressWarnings("MemberName")
public final class PaymentMethodsGroup extends ModelObject {
    @NonNull
    public static final Creator<PaymentMethodsGroup> CREATOR = new Creator<>(PaymentMethodsGroup.class);

    private static final String GROUP_TYPE = "groupType";
    private static final String NAME = "name";
    private static final String TYPES = "types";

    @NonNull
    public static final Serializer<PaymentMethodsGroup> SERIALIZER = new Serializer<PaymentMethodsGroup>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull PaymentMethodsGroup modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(GROUP_TYPE, modelObject.getGroupType());
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(TYPES, JsonUtils.serializeOptStringList(modelObject.getTypes()));
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethodsGroup.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public PaymentMethodsGroup deserialize(@NonNull JSONObject jsonObject) {
            final PaymentMethodsGroup paymentMethodsGroup = new PaymentMethodsGroup();
            paymentMethodsGroup.setGroupType(jsonObject.optString(GROUP_TYPE, null));
            paymentMethodsGroup.setName(jsonObject.optString(NAME, null));
            paymentMethodsGroup.setTypes(JsonUtils.parseOptStringList(jsonObject.optJSONArray(TYPES)));
            return paymentMethodsGroup;
        }
    };

    private String groupType;
    private String name;
    private List<String> types;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getGroupType() {
        return groupType;
    }

    @NonNull
    public String getName() {
        return name;
    }

    @Nullable
    public List<String> getTypes() {
        return types;
    }

    public void setGroupType(@Nullable String groupType) {
        this.groupType = groupType;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setTypes(@Nullable List<String> types) {
        this.types = types;
    }
}
