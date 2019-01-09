/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.Address;
import com.adyen.checkout.core.model.KlarnaSsnLookupResponse;
import com.adyen.checkout.core.model.Name;

import org.json.JSONException;
import org.json.JSONObject;

public class AddressAndName extends JsonObject implements KlarnaSsnLookupResponse {
    @NonNull
    public static final Parcelable.Creator<AddressAndName> CREATOR = new DefaultCreator<>(AddressAndName.class);

    private static final String KEY_ADDRESS = "address";
    private static final String KEY_NAME = "name";

    private Address mAddress;
    private Name mName;

    protected AddressAndName(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mAddress = parse(KEY_ADDRESS, Address.class);
        mName = parse(KEY_NAME, NameImpl.class);
    }

    @NonNull
    @Override
    public Address getAddress() {
        return mAddress;
    }

    @NonNull
    @Override
    public Name getName() {
        return mName;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AddressAndName that = (AddressAndName) o;

        if (mAddress != null ? !mAddress.equals(that.mAddress) : that.mAddress != null) {
            return false;
        }
        return mName != null ? mName.equals(that.mName) : that.mName == null;
    }

    @Override
    public int hashCode() {
        int result = mAddress != null ? mAddress.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mName != null ? mName.hashCode() : 0);
        return result;
    }
}
