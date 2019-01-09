/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/11/2018.
 */

package com.adyen.checkout.ui.internal.openinvoice.control;

import android.support.annotation.NonNull;

/**
 * This interface is a {@link ValidationChanger} that also specifies in the type of validation error.
 * Intended to use on individual field validation.
 */
public interface ValidationCheckDelegate extends ValidationChanger {

    /**
     * @return The type of error.
     */
    @NonNull
    ValidationState getValidationState();

    /**
     * Enum that indicates the current validation state of a {@link ValidationCheckDelegate}.
     */
    enum ValidationState {
        /** Input is valid. */
        VALID,
        /** Field is empty and needs to be filled in. */
        FIELD_EMPTY,
        /** The input format is incorrect. */
        INCORRECT_FORMAT
    }
}
