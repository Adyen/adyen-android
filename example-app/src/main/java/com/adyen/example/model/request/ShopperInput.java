/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.example.model.request;

import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import java.io.Serializable;


public final class ShopperInput implements Serializable {

    @Json(name = "personalDetails")
    private FieldVisibility mPersonalDetails;

    @Json(name = "billingAddress")
    private FieldVisibility mBillingAddress;

    @Json(name = "deliveryAddress")
    private FieldVisibility mDeliveryAddress;

    @Nullable
    public FieldVisibility getPersonalDetails() {
        return mPersonalDetails;
    }

    @Nullable
    public FieldVisibility getBillingAddress() {
        return mBillingAddress;
    }

    @Nullable
    public FieldVisibility getDeliveryAddress() {
        return mDeliveryAddress;
    }

    public void setPersonalDetails(@Nullable FieldVisibility personalDetails) {
        mPersonalDetails = personalDetails;
    }

    public void setBillingAddress(@Nullable FieldVisibility billingAddress) {
        mBillingAddress = billingAddress;
    }

    public void setDeliveryAddress(@Nullable FieldVisibility deliveryAddress) {
        mDeliveryAddress = deliveryAddress;
    }

    public enum FieldVisibility {
        @Json(name = "hidden")
        HIDDEN,
        @Json(name = "readOnly")
        READ_ONLY,
        @Json(name = "editable")
        EDITABLE
    }
}
