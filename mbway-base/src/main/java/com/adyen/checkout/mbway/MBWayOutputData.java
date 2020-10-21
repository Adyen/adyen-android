/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */

package com.adyen.checkout.mbway;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.util.ValidationUtils;
import com.adyen.checkout.base.validation.ValidatedField;

@SuppressWarnings("AbbreviationAsWordInName")
class MBWayOutputData implements OutputData {

    private final ValidatedField<String> mEmailField;
    private final ValidatedField<String> mMobilePhoneNumberField;

    private static ValidatedField<String> validateEmail(@NonNull String email) {
        if (!TextUtils.isEmpty(email) && ValidationUtils.isEmailValid(email)) {
            return new ValidatedField<>(email, ValidatedField.Validation.VALID);
        } else {
            return new ValidatedField<>(email, ValidatedField.Validation.INVALID);
        }
    }

    private static ValidatedField<String> validateMobileNumber(@NonNull String mobileNumber) {
        if (!TextUtils.isEmpty(mobileNumber) && ValidationUtils.isPhoneNumberValid(mobileNumber)) {
            return new ValidatedField<>(mobileNumber, ValidatedField.Validation.VALID);
        } else {
            return new ValidatedField<>(mobileNumber, ValidatedField.Validation.INVALID);
        }
    }

    MBWayOutputData(@NonNull String email, @NonNull String mobilePhoneNumber) {
        mEmailField = validateEmail(email);
        mMobilePhoneNumberField =  validateMobileNumber(mobilePhoneNumber);
    }

    @Override
    public boolean isValid() {
        return mEmailField.isValid() && mMobilePhoneNumberField.isValid();
    }

    @NonNull
    public ValidatedField<String> getEmailField() {
        return mEmailField;
    }

    @NonNull
    public ValidatedField<String> getMobilePhoneNumberField() {
        return mMobilePhoneNumberField;
    }
}
