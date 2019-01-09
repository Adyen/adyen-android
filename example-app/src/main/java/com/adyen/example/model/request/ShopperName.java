/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 16/11/2018.
 */

package com.adyen.example.model.request;

import com.squareup.moshi.Json;

import java.io.Serializable;

public class ShopperName implements Serializable {

    @Json(name = "firstName")
    private String mFirstName;

    @Json(name = "infix")
    private String mInfix;

    @Json(name = "lastName")
    private String mLastName;

    @Json(name = "gender")
    private Gender mGender;


    public void setFirstName(String firstName) {
        this.mFirstName = firstName;
    }

    public void setInfix(String infix) {
        this.mInfix = infix;
    }

    public void setLastName(String lastName) {
        this.mLastName = lastName;
    }

    public void setGender(Gender gender) {
        this.mGender = gender;
    }


    public enum Gender {
        @Json(name = "M")
        MALE,
        @Json(name = "F")
        FEMALE
    }

}
