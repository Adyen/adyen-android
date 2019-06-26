/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 19/3/2019.
 */

package com.adyen.checkout.base.component.data.output;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.component.validator.ValidationResult;

public class BaseField<T> implements Field<T> {
    private T mValue;
    private T mDisplayValue;
    private ValidationResult mValidationResult;

    /**
     * Constructs a {@link BaseField} object.
     *
     * @param value            {@link T}
     * @param displayValue     {@link T}
     * @param validationResult {@link ValidationResult}
     */
    public BaseField(@NonNull T value, @NonNull T displayValue, @NonNull ValidationResult validationResult) {
        mValue = value;
        mDisplayValue = displayValue;
        mValidationResult = validationResult;
    }

    @NonNull
    @Override
    public T getValue() {
        return mValue;
    }

    @Override
    public void setValue(@NonNull T value) {
        mValue = value;
    }

    @NonNull
    @Override
    public T getDisplayValue() {
        return mDisplayValue;
    }

    @Override
    public void setDisplayValue(@NonNull T displayValue) {
        mDisplayValue = displayValue;
    }

    @NonNull
    @Override
    public ValidationResult getValidationResult() {
        return mValidationResult;
    }

    @Override
    public void setValidationResult(@NonNull ValidationResult validationResult) {
        mValidationResult = validationResult;
    }
}
