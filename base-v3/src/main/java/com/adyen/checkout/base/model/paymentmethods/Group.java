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

@SuppressWarnings({"MemberName", "PMD.DataClass"})
public final class Group extends ModelObject {
    @NonNull
    public static final Creator<Group> CREATOR = new Creator<>(Group.class);

    private static final String NAME = "name";
    private static final String PAYMENT_METHOD_DATA = "paymentMethodData";
    private static final String TYPE = "type";

    @NonNull
    public static final Serializer<Group> SERIALIZER = new Serializer<Group>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Group modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(PAYMENT_METHOD_DATA, modelObject.getPaymentMethodData());
                jsonObject.putOpt(TYPE, modelObject.getType());
            } catch (JSONException e) {
                throw new ModelSerializationException(Group.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Group deserialize(@NonNull JSONObject jsonObject) {
            final Group group = new Group();
            group.setName(jsonObject.optString(NAME, null));
            group.setPaymentMethodData(jsonObject.optString(PAYMENT_METHOD_DATA, null));
            group.setType(jsonObject.optString(TYPE, null));
            return group;
        }
    };

    private String name;
    private String paymentMethodData;
    private String type;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nullable
    public String getPaymentMethodData() {
        return paymentMethodData;
    }

    @Nullable
    public String getType() {
        return type;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setPaymentMethodData(@Nullable String paymentMethodData) {
        this.paymentMethodData = paymentMethodData;
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }
}
