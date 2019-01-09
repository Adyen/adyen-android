/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 28/05/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.RedirectData;

import org.json.JSONException;
import org.json.JSONObject;

@ProvidedBy(ThreeDSecureRedirectData.class)
public final class ThreeDSecureRedirectData extends JsonObject implements RedirectData {
    @NonNull
    public static final Parcelable.Creator<ThreeDSecureRedirectData> CREATOR = new DefaultCreator<>(ThreeDSecureRedirectData.class);

    private final String mMd;

    private ThreeDSecureRedirectData(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mMd = jsonObject.getString("MD");
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ThreeDSecureRedirectData that = (ThreeDSecureRedirectData) o;

        return mMd != null ? mMd.equals(that.mMd) : that.mMd == null;
    }

    @Override
    public int hashCode() {
        return mMd != null ? mMd.hashCode() : 0;
    }

    @NonNull
    public String getMd() {
        return mMd;
    }
}
