/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 09/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.AndroidPayConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @deprecated Android Pay has been deprecated in favor of Google Pay.
 */
@Deprecated
public final class AndroidPayConfigurationImpl extends JsonObject implements AndroidPayConfiguration {
    @NonNull
    public static final Parcelable.Creator<AndroidPayConfigurationImpl> CREATOR = new DefaultCreator<>(AndroidPayConfigurationImpl.class);

    private final int mEnvironment;

    private final String mMerchantName;

    private final String mPublicKey;

    private AndroidPayConfigurationImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mEnvironment = jsonObject.getInt("environment");
        mMerchantName = jsonObject.getString("merchantName");
        mPublicKey = jsonObject.getString("publicKey");
    }

    @Override
    public int getEnvironment() {
        return mEnvironment;
    }

    @NonNull
    @Override
    public String getMerchantName() {
        return mMerchantName;
    }

    @NonNull
    @Override
    public String getPublicKey() {
        return mPublicKey;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AndroidPayConfigurationImpl that = (AndroidPayConfigurationImpl) o;

        if (mEnvironment != that.mEnvironment) {
            return false;
        }
        if (mMerchantName != null ? !mMerchantName.equals(that.mMerchantName) : that.mMerchantName != null) {
            return false;
        }
        return mPublicKey != null ? mPublicKey.equals(that.mPublicKey) : that.mPublicKey == null;
    }

    @Override
    public int hashCode() {
        int result = mEnvironment;
        result = HashUtils.MULTIPLIER * result + (mMerchantName != null ? mMerchantName.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPublicKey != null ? mPublicKey.hashCode() : 0);
        return result;
    }
}
