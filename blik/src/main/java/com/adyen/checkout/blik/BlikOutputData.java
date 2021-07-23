/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.OutputData;
import com.adyen.checkout.components.ui.FieldState;
import com.adyen.checkout.components.ui.Validation;
import com.adyen.checkout.core.log.LogUtil;
import com.adyen.checkout.core.log.Logger;

class BlikOutputData implements OutputData {
    private static final String TAG = LogUtil.getTag();

    private static final int BLIK_CODE_LENGTH = 6;

    private final FieldState<String> mBlikCodeField;

    BlikOutputData(@NonNull String blikCode) {
        mBlikCodeField = new FieldState<>(blikCode, getBlikCodeValidation(blikCode));
    }

    @Override
    public boolean isValid() {
        return mBlikCodeField.getValidation().isValid();
    }

    @NonNull
    public FieldState<String> getBlikCodeField() {
        return mBlikCodeField;
    }

    private Validation getBlikCodeValidation(@NonNull String blikCode) {
        try {
            Integer.parseInt(blikCode);
        } catch (NumberFormatException e) {
            Logger.e(TAG, "Failed to parse blik code to Integer", e);
            return new Validation.Invalid(R.string.checkout_blik_code_not_valid);
        }
        if (blikCode.length() == BLIK_CODE_LENGTH) {
            return Validation.Valid.INSTANCE;
        }
        return new Validation.Invalid(R.string.checkout_blik_code_not_valid);
    }
}
