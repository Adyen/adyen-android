/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 6/11/2020.
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

class Issuer extends ModelObject {
    @NonNull
    public static final Creator<Issuer> CREATOR = new Creator<>(Issuer.class);

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ENABLED = "enabled";

    @NonNull
    public static final Serializer<Issuer> SERIALIZER = new Serializer<Issuer>() {

        @NonNull
        @Override
        public JSONObject serialize(@NonNull Issuer modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ID, modelObject.getId());
                jsonObject.putOpt(NAME, modelObject.getName());
                jsonObject.putOpt(ENABLED, modelObject.isEnabled());
            } catch (JSONException e) {
                throw new ModelSerializationException(PaymentMethod.class, e);
            }
            return jsonObject;
        }

        @NonNull
        @Override
        public Issuer deserialize(@NonNull JSONObject jsonObject) {
            final Issuer issuer = new Issuer();
            issuer.setId(jsonObject.optString(ID, null));
            issuer.setName(jsonObject.optString(NAME, null));
            issuer.setEnabled(jsonObject.optBoolean(ENABLED, true));
            return issuer;
        }
    };

    private String id;
    private String name;
    private boolean enabled;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public String getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
