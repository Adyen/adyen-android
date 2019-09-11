/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 30/8/2019.
 */

package com.adyen.checkout.base.validation;

import android.support.annotation.NonNull;

public class ValidatedField<T> {

    private final T mValue;
    private final Validation mValidation;

    public ValidatedField(@NonNull T value, @NonNull Validation validation) {
        mValue = value;
        mValidation = validation;
    }

    public boolean isValid() {
        return mValidation == Validation.VALID;
    }

    @NonNull
    public T getValue() {
        return mValue;
    }

    @NonNull
    public ValidatedField.Validation getValidation() {
        return mValidation;
    }

    public enum Validation {
        VALID,
        PARTIAL,
        INVALID
    }
}
