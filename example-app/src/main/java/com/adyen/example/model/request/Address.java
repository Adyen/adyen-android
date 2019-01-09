/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 15/11/2018.
 */

package com.adyen.example.model.request;

import com.squareup.moshi.Json;

public class Address {

    @Json(name = "street")
    private String mStreet;

    @Json(name = "houseNumberOrName")
    private String mHouseNumberOrName;

    @Json(name = "city")
    private String mCity;

    @Json(name = "country")
    private String mCountry;

    @Json(name = "postalCode")
    private String mPostalCode;

    @Json(name = "stateOrProvince")
    private String mStateOrProvince;

    public void setStreet(String street) {
        this.mStreet = street;
    }

    public void setHouseNumberOrName(String houseNumberOrName) {
        this.mHouseNumberOrName = houseNumberOrName;
    }

    public void setCity(String city) {
        this.mCity = city;
    }

    public void setCountry(String country) {
        this.mCountry = country;
    }

    public void setPostalCode(String postalCode) {
        this.mPostalCode = postalCode;
    }

    public void setStateOrProvince(String stateOrProvince) {
        this.mStateOrProvince = stateOrProvince;
    }
}
