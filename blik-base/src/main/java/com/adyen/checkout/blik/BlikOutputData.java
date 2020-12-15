/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.adyen.checkout.base.component.OutputData;
import com.adyen.checkout.base.validation.ValidatedField;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

class BlikOutputData implements OutputData {
    private static final String TAG = LogUtil.getTag();

    private static final int BLIK_CODE_LENGTH = 6;

    private final ValidatedField<String> mBlikCodeField;

    BlikOutputData(@NonNull String blikCode) {
        mBlikCodeField = new ValidatedField<>(blikCode, getBlikCodeValidation(blikCode));
    }

    @Override
    public boolean isValid() {
        return mBlikCodeField.isValid();
    }

    @NonNull
    public ValidatedField<String> getBlikCodeField() {
        return mBlikCodeField;
    }

    private ValidatedField.Validation getBlikCodeValidation(@NonNull String blikCode) {
        if (TextUtils.isEmpty(blikCode)) {
            return ValidatedField.Validation.PARTIAL;
        }
        try {
            Integer.parseInt(blikCode);
        } catch (NumberFormatException e) {
            Logger.e(TAG, "Failed to parse blik code to Integer", e);
            return ValidatedField.Validation.INVALID;
        }
        if (blikCode.length() == BLIK_CODE_LENGTH) {
            return ValidatedField.Validation.VALID;
        }
        if (blikCode.length() < BLIK_CODE_LENGTH) {
            return ValidatedField.Validation.PARTIAL;
        }
        return ValidatedField.Validation.INVALID;
    }
}
