/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 10/07/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.base.internal.HashUtils;
import com.adyen.checkout.core.model.WeChatPaySdkRedirectData;

import org.json.JSONException;
import org.json.JSONObject;

public final class WeChatPaySdkRedirectDataImpl extends JsonObject implements WeChatPaySdkRedirectData {
    @NonNull
    public static final Parcelable.Creator<WeChatPaySdkRedirectDataImpl> CREATOR = new DefaultCreator<>(WeChatPaySdkRedirectDataImpl.class);

    private final String mAppId;

    private final String mPartnerId;

    private final String mPrepayId;

    private final String mTimestamp;

    private final String mPackageValue;

    private final String mNonceStr;

    private final String mSignature;

    private WeChatPaySdkRedirectDataImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mAppId = jsonObject.getString("appid");
        mPartnerId = jsonObject.getString("partnerid");
        mPrepayId = jsonObject.getString("prepayid");
        mTimestamp = jsonObject.getString("timestamp");
        mPackageValue = jsonObject.getString("package");
        mNonceStr = jsonObject.getString("noncestr");
        mSignature = jsonObject.getString("sign");
    }

    @NonNull
    @Override
    public String getAppId() {
        return mAppId;
    }

    @NonNull
    @Override
    public String getPartnerId() {
        return mPartnerId;
    }

    @NonNull
    @Override
    public String getPrepayId() {
        return mPrepayId;
    }

    @NonNull
    @Override
    public String getTimestamp() {
        return mTimestamp;
    }

    @NonNull
    @Override
    public String getPackageValue() {
        return mPackageValue;
    }

    @NonNull
    @Override
    public String getNonceStr() {
        return mNonceStr;
    }

    @NonNull
    @Override
    public String getSignature() {
        return mSignature;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WeChatPaySdkRedirectDataImpl that = (WeChatPaySdkRedirectDataImpl) o;

        if (mAppId != null ? !mAppId.equals(that.mAppId) : that.mAppId != null) {
            return false;
        }
        if (mPartnerId != null ? !mPartnerId.equals(that.mPartnerId) : that.mPartnerId != null) {
            return false;
        }
        if (mPrepayId != null ? !mPrepayId.equals(that.mPrepayId) : that.mPrepayId != null) {
            return false;
        }
        if (mTimestamp != null ? !mTimestamp.equals(that.mTimestamp) : that.mTimestamp != null) {
            return false;
        }
        if (mPackageValue != null ? !mPackageValue.equals(that.mPackageValue) : that.mPackageValue != null) {
            return false;
        }
        if (mNonceStr != null ? !mNonceStr.equals(that.mNonceStr) : that.mNonceStr != null) {
            return false;
        }
        return mSignature != null ? mSignature.equals(that.mSignature) : that.mSignature == null;
    }

    @Override
    public int hashCode() {
        int result = mAppId != null ? mAppId.hashCode() : 0;
        result = HashUtils.MULTIPLIER * result + (mPartnerId != null ? mPartnerId.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPrepayId != null ? mPrepayId.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mTimestamp != null ? mTimestamp.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mPackageValue != null ? mPackageValue.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mNonceStr != null ? mNonceStr.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mSignature != null ? mSignature.hashCode() : 0);
        return result;
    }
}
