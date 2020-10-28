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

import java.util.Calendar;

class AfterPayPersonalDataOutputData implements OutputData {

    private final ValidatedField<String> mFirstNameField;
    private final ValidatedField<String> mLastNameField;
    private final ValidatedField<Gender> mGenderField;
    private final ValidatedField<Calendar> mDateOfBirthField;
    private final ValidatedField<String> mTelephoneNumberField;
    private final ValidatedField<String> mShopperEmailField;


    AfterPayPersonalDataOutputData(@NonNull ValidatedField<String> firstNameField,
            @NonNull ValidatedField<String> lastNameField, @NonNull ValidatedField<Gender> genderField,
            @NonNull ValidatedField<Calendar> dateOfBirthField, @NonNull ValidatedField<String> telephoneNumberField,
            @NonNull ValidatedField<String> shopperEmailField) {
        mFirstNameField = firstNameField;
        mLastNameField = lastNameField;
        mGenderField = genderField;
        mDateOfBirthField = dateOfBirthField;
        mTelephoneNumberField = telephoneNumberField;
        mShopperEmailField = shopperEmailField;
    }

    @Override
    public boolean isValid() {
        return mFirstNameField.isValid() && mLastNameField.isValid() && mGenderField.isValid();
    }

    @NonNull
    public ValidatedField<String> getFirstNameField() {
        return mFirstNameField;
    }

    @NonNull
    public ValidatedField<String> getLastNameField() {
        return mLastNameField;
    }

    @NonNull
    public ValidatedField<Gender> getGenderField() {
        return mGenderField;
    }

    @NonNull
    public ValidatedField<Calendar> getDateOfBirthField() {
        return mDateOfBirthField;
    }

    @NonNull
    public ValidatedField<String> getTelephoneNumberField() {
        return mTelephoneNumberField;
    }

    @NonNull
    public ValidatedField<String> getShopperEmailField() {
        return mShopperEmailField;
    }
}
