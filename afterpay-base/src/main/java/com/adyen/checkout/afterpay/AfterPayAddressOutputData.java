/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 9/12/2019.
 */

package com.adyen.checkout.afterpay;

import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;

import java.util.Locale;

class AfterPayAddressOutputData implements OutputData {

    private final ValidatedField<String> mStreet;
    private final ValidatedField<String> mHouseNumberOrName;
    private final ValidatedField<String> mCity;
    private final ValidatedField<String> mPostalCode;
    private final ValidatedField<String> mStateOrProvince;
    private final ValidatedField<Locale> mLocale;

    /**
     * Constructs a {@link com.adyen.checkout.afterpay.AfterPayComponent} object.
     */

    AfterPayAddressOutputData(@NonNull ValidatedField<String> street,
            @NonNull ValidatedField<String> houseNumberOrName, @NonNull ValidatedField<String> city,
            @NonNull ValidatedField<String> postalCode, @NonNull ValidatedField<String> stateOrProvince,
            @NonNull ValidatedField<Locale> locale) {
        mStreet = street;
        mHouseNumberOrName = houseNumberOrName;
        mCity = city;
        mPostalCode = postalCode;
        mStateOrProvince = stateOrProvince;
        mLocale = locale;
    }


    @Override
    public boolean isValid() {
        return mStreet.isValid() && mHouseNumberOrName.isValid() && mCity.isValid() && mPostalCode.isValid() && mStateOrProvince.isValid()
                && mLocale.isValid();
    }

    @NonNull
    public ValidatedField<String> getStreet() {
        return mStreet;
    }

    @NonNull
    public ValidatedField<String> getHouseNumberOrName() {
        return mHouseNumberOrName;
    }

    @NonNull
    public ValidatedField<String> getCity() {
        return mCity;
    }

    @NonNull
    public ValidatedField<String> getPostalCode() {
        return mPostalCode;
    }

    @NonNull
    public ValidatedField<String> getStateOrProvince() {
        return mStateOrProvince;
    }

    @NonNull
    public ValidatedField<Locale> getLocale() {
        return mLocale;
    }
}
