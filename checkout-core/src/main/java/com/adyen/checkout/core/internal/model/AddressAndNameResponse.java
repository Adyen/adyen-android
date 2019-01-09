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

import java.util.List;

public class AddressAndNameResponse extends JsonObject {
    @NonNull
    public static final Parcelable.Creator<AddressAndNameResponse> CREATOR = new DefaultCreator<>(AddressAndNameResponse.class);

    private static final String KEY_ADDRESS_NAMES = "addressNames";

    private List<AddressAndNameWrapper> mAddressAndNameWrappers;

    protected AddressAndNameResponse(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mAddressAndNameWrappers = parseList(KEY_ADDRESS_NAMES, AddressAndNameWrapper.class);
    }

    @NonNull
    public List<AddressAndNameWrapper> getAddressAndNameWrappers() {
        return mAddressAndNameWrappers;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddressAndNameResponse that = (AddressAndNameResponse) o;

        return mAddressAndNameWrappers != null ? mAddressAndNameWrappers.equals(that.mAddressAndNameWrappers) : that.mAddressAndNameWrappers == null;
    }

    @Override
    public int hashCode() {
        return mAddressAndNameWrappers != null ? mAddressAndNameWrappers.hashCode() : 0;
    }
}
