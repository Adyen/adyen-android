package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.InputDetail;
import com.adyen.checkout.core.model.Item;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/07/2018.
 */
public final class InputDetailImpl extends JsonObject implements InputDetail {
    public static final Parcelable.Creator<InputDetailImpl> CREATOR = new DefaultCreator<>(InputDetailImpl.class);

    private static final String KEY_KEY = "key";

    private static final String KEY_TYPE = "type";

    private static final String KEY_OPTIONAL = "optional";

    private static final String KEY_VALUE = "value";

    private static final String KEY_ITEMS = "items";

    @Nullable
    public static InputDetail findByKey(@Nullable List<InputDetail> inputDetails, @NonNull String key) {
        if (inputDetails != null) {
            for (InputDetail inputDetail : inputDetails) {
                if (key.equals(inputDetail.getKey())) {
                    return inputDetail;
                }
            }
        }

        return null;
    }

    private final String mKey;

    private final Type mType;

    private final Boolean mOptional;

    private final String mValue;

    private final List<ItemImpl> mItems;

    private InputDetailImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mKey = jsonObject.getString(KEY_KEY);
        mType = parseEnum(KEY_TYPE, Type.class);
        mOptional = jsonObject.has(KEY_OPTIONAL) ? jsonObject.getBoolean(KEY_OPTIONAL) : null;
        mValue = jsonObject.optString(KEY_VALUE, null);
        mItems = parseOptionalList(KEY_ITEMS, ItemImpl.class);
    }

    @NonNull
    @Override
    public String getKey() {
        return mKey;
    }

    @NonNull
    @Override
    public Type getType() {
        return mType;
    }

    @Override
    public boolean isOptional() {
        return Boolean.TRUE.equals(mOptional);
    }

    @Nullable
    @Override
    public List<Item> getItems() {
        return mItems != null ? new ArrayList<Item>(mItems) : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        InputDetailImpl that = (InputDetailImpl) o;

        if (mKey != null ? !mKey.equals(that.mKey) : that.mKey != null) {
            return false;
        }
        if (mType != that.mType) {
            return false;
        }
        if (mOptional != null ? !mOptional.equals(that.mOptional) : that.mOptional != null) {
            return false;
        }
        if (mValue != null ? !mValue.equals(that.mValue) : that.mValue != null) {
            return false;
        }
        return mItems != null ? mItems.equals(that.mItems) : that.mItems == null;
    }

    @Override
    public int hashCode() {
        int result = mKey != null ? mKey.hashCode() : 0;
        result = 31 * result + (mType != null ? mType.hashCode() : 0);
        result = 31 * result + (mOptional != null ? mOptional.hashCode() : 0);
        result = 31 * result + (mValue != null ? mValue.hashCode() : 0);
        result = 31 * result + (mItems != null ? mItems.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InputDetail{" + "Key='" + mKey + '\'' + ", Optional=" + mOptional + '}';
    }

    @Nullable
    public String getValue() {
        return mValue;
    }
}
