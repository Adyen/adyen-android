/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/10/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.FieldSetConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public final class FieldSetConfigurationImpl extends JsonObject implements FieldSetConfiguration {
    @NonNull
    public static final Parcelable.Creator<FieldSetConfigurationImpl> CREATOR = new DefaultCreator<>(FieldSetConfigurationImpl.class);

    private static final String FIELD_VISIBILITY_KEY = "fieldVisibility";

    private FieldVisibility mFieldVisibility;


    private FieldSetConfigurationImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mFieldVisibility = parseEnum(FIELD_VISIBILITY_KEY, FieldVisibility.class);
    }

    @NonNull
    @Override
    public FieldVisibility getFieldVisibility() {
        return mFieldVisibility;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FieldSetConfigurationImpl that = (FieldSetConfigurationImpl) o;

        return mFieldVisibility == that.mFieldVisibility;
    }

    @Override
    public int hashCode() {
        return mFieldVisibility != null ? mFieldVisibility.hashCode() : 0;
    }
}
