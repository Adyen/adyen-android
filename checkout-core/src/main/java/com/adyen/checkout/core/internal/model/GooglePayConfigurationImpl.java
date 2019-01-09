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
import com.adyen.checkout.core.model.GooglePayConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public final class GooglePayConfigurationImpl extends JsonObject implements GooglePayConfiguration {
    @NonNull
    public static final Parcelable.Creator<GooglePayConfigurationImpl> CREATOR = new DefaultCreator<>(GooglePayConfigurationImpl.class);

    private final int mEnvironment;

    private final String mGateway;

    private final String mGatewayMerchantId;

    private GooglePayConfigurationImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mEnvironment = jsonObject.getInt("environment");
        mGateway = jsonObject.getString("gateway");
        mGatewayMerchantId = jsonObject.getString("gatewayMerchantId");
    }

    @Override
    public int getEnvironment() {
        return mEnvironment;
    }

    @NonNull
    @Override
    public String getGateway() {
        return mGateway;
    }

    @NonNull
    @Override
    public String getGatewayMerchantId() {
        return mGatewayMerchantId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        GooglePayConfigurationImpl that = (GooglePayConfigurationImpl) o;

        if (mEnvironment != that.mEnvironment) {
            return false;
        }
        if (mGateway != null ? !mGateway.equals(that.mGateway) : that.mGateway != null) {
            return false;
        }
        return mGatewayMerchantId != null ? mGatewayMerchantId.equals(that.mGatewayMerchantId) : that.mGatewayMerchantId == null;
    }

    @Override
    public int hashCode() {
        int result = mEnvironment;
        result = HashUtils.MULTIPLIER * result + (mGateway != null ? mGateway.hashCode() : 0);
        result = HashUtils.MULTIPLIER * result + (mGatewayMerchantId != null ? mGatewayMerchantId.hashCode() : 0);
        return result;
    }
}
