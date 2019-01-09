/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 10/12/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.Name;

import org.json.JSONException;
import org.json.JSONObject;

public class NameImpl extends JsonObject implements Name {
    @NonNull
    public static final Parcelable.Creator<NameImpl> CREATOR = new DefaultCreator<>(NameImpl.class);

    private static final String KEY_FIRST_NAME = "firstName";
    private static final String KEY_LAST_NAME = "lastName";

    private String mFirstName;

    private String mLastName;

    protected NameImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mFirstName = jsonObject.getString(KEY_FIRST_NAME);
        mLastName = jsonObject.getString(KEY_LAST_NAME);
    }

    @NonNull
    @Override
    public String getFirstName() {
        return mFirstName;
    }

    @NonNull
    @Override
    public String getLastName() {
        return mLastName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NameImpl name = (NameImpl) o;

        if (mFirstName != null ? !mFirstName.equals(name.mFirstName) : name.mFirstName != null) {
            return false;
        }
        return mLastName != null ? mLastName.equals(name.mLastName) : name.mLastName == null;
    }

    @Override
    public int hashCode() {
        int result = mFirstName != null ? mFirstName.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mLastName != null ? mLastName.hashCode() : 0);
        return result;
    }
}
