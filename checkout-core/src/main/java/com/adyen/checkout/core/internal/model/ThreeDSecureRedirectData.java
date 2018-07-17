package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.model.RedirectData;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 28/05/2018.
 */
@ProvidedBy(ThreeDSecureRedirectData.class)
public final class ThreeDSecureRedirectData extends JsonObject implements RedirectData {
    public static final Parcelable.Creator<ThreeDSecureRedirectData> CREATOR = new DefaultCreator<>(ThreeDSecureRedirectData.class);

    private final String mMd;

    private ThreeDSecureRedirectData(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mMd = jsonObject.getString("MD");
    }

    @Override
    public boolean equals(Object o) {
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
