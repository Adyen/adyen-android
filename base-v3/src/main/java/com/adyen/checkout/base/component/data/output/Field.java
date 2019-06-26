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

public interface Field<T> {
    @NonNull
    T getValue();

    void setValue(@NonNull T value);

    @NonNull
    T getDisplayValue();

    void setDisplayValue(@NonNull T displayValue);

    @NonNull
    ValidationResult getValidationResult();

    void setValidationResult(@NonNull ValidationResult validationResult);
}
