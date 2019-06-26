/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 18/3/2019.
 */

package com.adyen.checkout.base.component.validator;

import android.support.annotation.NonNull;

/**
 * Class holding the result of a validation.
 */
public class ValidationResult {
    private final Validity mValidity;

    protected ValidationResult(@NonNull Validity validity) {
        mValidity = validity;
    }

    @NonNull
    public Validity getValidity() {
        return mValidity;
    }

    public boolean isValid() {
        return mValidity == Validity.VALID;
    }
}
