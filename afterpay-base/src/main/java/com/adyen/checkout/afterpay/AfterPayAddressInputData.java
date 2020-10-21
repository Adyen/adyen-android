/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/12/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.InputData;

import java.util.Locale;

public class AfterPayAddressInputData implements InputData {

    private String mStreet = "";
    private String mHouseNumberOrName = "";
    private String mCity = "";
    private String mPostalCode = "";
    private String mStateOrProvince = "";
    private Locale mLocale = Locale.getDefault();

    @NonNull
    public String getStreet() {
        return mStreet;
    }

    public void setStreet(@NonNull String street) {
        mStreet = street;
    }

    @NonNull
    public String getHouseNumberOrName() {
        return mHouseNumberOrName;
    }

    public void setHouseNumberOrName(@NonNull String houseNumberOrName) {
        mHouseNumberOrName = houseNumberOrName;
    }

    @NonNull
    public String getCity() {
        return mCity;
    }

    public void setCity(@NonNull String city) {
        mCity = city;
    }

    @NonNull
    public String getPostalCode() {
        return mPostalCode;
    }

    public void setPostalCode(@NonNull String postalCode) {
        mPostalCode = postalCode;
    }

    @NonNull
    public String getStateOrProvince() {
        return mStateOrProvince;
    }

    public void setStateOrProvince(@NonNull String stateOrProvince) {
        mStateOrProvince = stateOrProvince;
    }

    @NonNull
    public Locale getLocale() {
        return mLocale;
    }

    public void setLocale(@NonNull Locale locale) {
        mLocale = locale;
    }
}
