/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/12/2020.
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
public final class InputDetail extends ModelObject {
    @NonNull
    public static final Creator<InputDetail> CREATOR = new Creator<>(InputDetail.class);

    private static final String ITEMS = "items";

    @NonNull
    public static final Serializer<InputDetail> SERIALIZER = new Serializer<InputDetail>() {
        @Override
        @NonNull
        public JSONObject serialize(@NonNull InputDetail modelObject) {
            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.putOpt(ITEMS, ModelUtils.serializeOptList(modelObject.getItems(), Item.SERIALIZER));
            } catch (JSONException e) {
                throw new ModelSerializationException(InputDetail.class, e);
            }
            return jsonObject;
        }

        @Override
        @NonNull
        public InputDetail deserialize(@NonNull JSONObject jsonObject) {
            final InputDetail inputDetail = new InputDetail();
            inputDetail.setItems(ModelUtils.deserializeOptList(jsonObject.optJSONArray(ITEMS), Item.SERIALIZER));
            return inputDetail;
        }
    };
    private List<Item> items;

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        JsonUtils.writeToParcel(dest, SERIALIZER.serialize(this));
    }

    @Nullable
    public List<Item> getItems() {
        return items;
    }

    public void setItems(@Nullable List<Item> items) {
        this.items = items;
    }
}
