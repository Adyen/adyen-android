/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 31/8/2020.
 */

package com.adyen.checkout.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Exception thrown when there is an issue with an internal API call.
 */
public class ApiCallException extends CheckoutException {

    private static final long serialVersionUID = 4060450855496938503L;

    public ApiCallException(@NonNull String errorMessage) {
        super(errorMessage);
    }

    public ApiCallException(@NonNull String errorMessage, @Nullable Throwable cause) {
        super(errorMessage, cause);
    }
}
