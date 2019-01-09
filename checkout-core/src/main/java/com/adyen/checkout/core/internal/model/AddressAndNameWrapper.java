/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 14/11/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This intermediary data class is received in the {@link AddressAndNameResponse}
 * and contains the actually useful {@link AddressAndName}.
 */
public class AddressAndNameWrapper extends JsonObject {
    @NonNull
    public static final Parcelable.Creator<AddressAndNameWrapper> CREATOR = new DefaultCreator<>(AddressAndNameWrapper.class);

    private static final String KEY_ADDRESS_NAME = "AddressName";

    private AddressAndName mAddressAndName;

    protected AddressAndNameWrapper(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mAddressAndName = parse(KEY_ADDRESS_NAME, AddressAndName.class);
    }

    @NonNull
    public AddressAndName getAddressAndName() {
        return mAddressAndName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddressAndNameWrapper that = (AddressAndNameWrapper) o;

        return mAddressAndName != null ? mAddressAndName.equals(that.mAddressAndName) : that.mAddressAndName == null;
    }

    @Override
    public int hashCode() {
        return mAddressAndName != null ? mAddressAndName.hashCode() : 0;
    }
}
