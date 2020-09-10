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

@SuppressWarnings("MemberName")
public final class Item extends ModelObject {
    @NonNull
    public static final Creator<Item> CREATOR = new Creator<>(Item.class);

    private static final String ID = "id";
    private static final String NAME = "name";

    @NonNull
    public static final Serializer<Item> SERIALIZER = new Serializer<Item>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull Item modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ID, modelObject.getId());
                jsonObject.putOpt(NAME, modelObject.getName());
            } catch (JSONException e) {
                throw new ModelSerializationException(Item.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public Item deserialize(@NonNull JSONObject jsonObject) {
            final Item item = new Item();
            item.setId(jsonObject.optString(ID, null));
            item.setName(jsonObject.optString(NAME, null));
            return item;
        }
    };

    private String id;
    private String name;

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

    public void setId(@Nullable String id) {
        this.id = id;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }
}
