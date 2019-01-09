/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 01/11/2018.
 */

package com.adyen.checkout.core.internal.model;

import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.adyen.checkout.base.internal.JsonObject;
import com.adyen.checkout.core.model.SurchargeConfiguration;

import org.json.JSONException;
import org.json.JSONObject;

public final class SurchargeConfigurationImpl extends JsonObject implements SurchargeConfiguration {
    @NonNull
    public static final Parcelable.Creator<SurchargeConfigurationImpl> CREATOR = new DefaultCreator<>(SurchargeConfigurationImpl.class);

    private final String mSurchargeCurrencyCode;

    private final int mSurchargeVariableCost;

    private final long mSurchargeFixedCost;

    private final long mSurchargeFinalAmount;

    private final long mSurchargeTotalCost;

    public SurchargeConfigurationImpl(@NonNull JSONObject jsonObject) throws JSONException {
        super(jsonObject);

        mSurchargeCurrencyCode = jsonObject.getString("surchargeCurrencyCode");
        String surchargeVariableCost = jsonObject.optString("surchargeVariableCost");
        mSurchargeVariableCost = null == surchargeVariableCost || "null".equals(surchargeVariableCost)
                ? 0
                : Integer.parseInt(surchargeVariableCost);
        String surchargeFixedCost = jsonObject.optString("surchargeFixedCost");
        mSurchargeFixedCost = null == surchargeFixedCost || "null".equals(surchargeFixedCost)
                ? 0
                : Long.parseLong(surchargeFixedCost);
        mSurchargeFinalAmount = jsonObject.getLong("surchargeFinalAmount");
        mSurchargeTotalCost = jsonObject.getLong("surchargeTotalCost");
    }

    @NonNull
    @Override
    public String getSurchargeCurrencyCode() {
        return mSurchargeCurrencyCode;
    }

    @Override
    public long getSurchargeFixedCost() {
        return mSurchargeFixedCost;
    }

    @Override
    public int getSurchargeVariableCost() {
        return mSurchargeVariableCost;
    }

    @Override
    public long getSurchargeTotalCost() {
        return mSurchargeTotalCost;
    }

    @Override
    public long getSurchargeFinalAmount() {
        return mSurchargeFinalAmount;
    }
}
