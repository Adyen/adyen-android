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

class BlikOutputData implements OutputData {

    private final ValidatedField<String> mBlikCodeField;

    BlikOutputData(@NonNull String blikCode) {
        mBlikCodeField = new ValidatedField<>(blikCode,
                TextUtils.isEmpty(blikCode) ? ValidatedField.Validation.PARTIAL : ValidatedField.Validation.VALID);
    }

    @Override
    public boolean isValid() {
        return mBlikCodeField.isValid();
    }

    @NonNull
    public ValidatedField<String> getBlikCodeField() {
        return mBlikCodeField;
    }
}