/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */

package com.adyen.checkout.mbway;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.OutputData;
import com.adyen.checkout.components.util.ValidationUtils;
import com.adyen.checkout.components.validation.ValidatedField;

@SuppressWarnings("AbbreviationAsWordInName")
class MBWayOutputData implements OutputData {

    private final ValidatedField<String> mMobilePhoneNumberField;

    private static ValidatedField<String> validateMobileNumber(@NonNull String mobileNumber) {
        if (!TextUtils.isEmpty(mobileNumber) && ValidationUtils.isPhoneNumberValid(mobileNumber)) {
            return new ValidatedField<>(mobileNumber, ValidatedField.Validation.VALID);
        } else {
            return new ValidatedField<>(mobileNumber, ValidatedField.Validation.INVALID);
        }
    }

    MBWayOutputData(@NonNull String mobilePhoneNumber) {
        mMobilePhoneNumberField = validateMobileNumber(mobilePhoneNumber);
    }

    @Override
    public boolean isValid() {
        return mMobilePhoneNumberField.isValid();
    }

    @NonNull
    public ValidatedField<String> getMobilePhoneNumberField() {
        return mMobilePhoneNumberField;
    }
}
