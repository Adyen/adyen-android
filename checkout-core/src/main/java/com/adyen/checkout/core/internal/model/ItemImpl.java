package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.Item;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 09/07/2018.
 */
public final class ItemImpl extends JsonObject implements Item {
    public static final Parcelable.Creator<ItemImpl> CREATOR = new DefaultCreator<>(ItemImpl.class);

    private static final String KEY_ID = "id";

    private static final String KEY_NAME = "name";

    private final String mId;

    private final String mName;

    private ItemImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mId = jsonObject.getString(KEY_ID);
        mName = jsonObject.getString(KEY_NAME);
    }

    @NonNull
    @Override
    public String getId() {
        return mId;
    }

    @NonNull
    @Override
    public String getName() {
        return mName;
    }

    @NonNull
    @Override
    public String getTxSubVariant() {
        return mId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItemImpl item = (ItemImpl) o;

        if (mId != null ? !mId.equals(item.mId) : item.mId != null) {
            return false;
        }
        return mName != null ? mName.equals(item.mName) : item.mName == null;
    }

    @Override
    public int hashCode() {
        int result = mId != null ? mId.hashCode() : 0;
        result = 31 * result + (mName != null ? mName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Item{" + "Name='" + mName + '\'' + '}';
    }
}
