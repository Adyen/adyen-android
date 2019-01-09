/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/10/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.KlarnaConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public class KlarnaConfigurationImpl extends JsonObject implements KlarnaConfiguration {
    @NonNull
    public static final Parcelable.Creator<KlarnaConfigurationImpl> CREATOR = new DefaultCreator<>(KlarnaConfigurationImpl.class);

    private static final String KEY_SSN_LOOKUP_UR = "shopperInfoSSNLookupUrl";

    private String mShopperInfoSsnLookupUrl;

    protected KlarnaConfigurationImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);
        mShopperInfoSsnLookupUrl = jsonObject.getString(KEY_SSN_LOOKUP_UR);
    }

    @NonNull
    public String getShopperInfoSsnLookupUrl() {
        return mShopperInfoSsnLookupUrl;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        KlarnaConfigurationImpl that = (KlarnaConfigurationImpl) o;

        return mShopperInfoSsnLookupUrl != null
                ? mShopperInfoSsnLookupUrl.equals(that.mShopperInfoSsnLookupUrl)
                : that.mShopperInfoSsnLookupUrl == null;
    }

    @Override
    public int hashCode() {
        return mShopperInfoSsnLookupUrl != null ? mShopperInfoSsnLookupUrl.hashCode() : 0;
    }
}
