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

import java.util.Calendar;

public class AfterPayPersonalDataInputData implements InputData {

    private String mFirstName = "";
    private String mLastName = "";
    private Gender mGender = Gender.U;
    private Calendar mDateOfBirth = Calendar.getInstance();
    private String mTelephoneNumber = "";
    private String mShopperEmail = "";

    @NonNull
    public String getFirstName() {
        return mFirstName;
    }

    public void setFirstName(@NonNull String firstName) {
        mFirstName = firstName;
    }

    @NonNull
    public String getLastName() {
        return mLastName;
    }

    public void setLastName(@NonNull String lastName) {
        mLastName = lastName;
    }

    @NonNull
    public Gender getGender() {
        return mGender;
    }

    public void setGender(@NonNull Gender gender) {
        mGender = gender;
    }

    @NonNull
    public Calendar getDateOfBirth() {
        return mDateOfBirth;
    }

    public void setDateOfBirth(@NonNull Calendar dateOfBirth) {
        mDateOfBirth = dateOfBirth;
    }

    @NonNull
    public String getTelephoneNumber() {
        return mTelephoneNumber;
    }

    public void setTelephoneNumber(@NonNull String telephoneNumber) {
        mTelephoneNumber = telephoneNumber;
    }

    @NonNull
    public String getShopperEmail() {
        return mShopperEmail;
    }

    public void setShopperEmail(@NonNull String shopperEmail) {
        mShopperEmail = shopperEmail;
    }
}
