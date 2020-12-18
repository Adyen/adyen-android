/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.exception;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ComponentException extends CheckoutException {

    private static final long serialVersionUID = -2906708092144840124L;

    public ComponentException(@NonNull String errorMessage) {
        super(errorMessage);
    }

    public ComponentException(@NonNull String errorMessage, @Nullable Throwable cause) {
        super(errorMessage, cause);
    }
}
