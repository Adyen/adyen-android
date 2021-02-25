/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/8/2019.
 */

package com.adyen.checkout.adyen3ds2.exception;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.ComponentException;

/**
 * This exception is just an indication that the 3DS2 Authentication did not finish as expected.
 * Can be caused by an actual error or by user cancellation.
 */
public class Authentication3DS2Exception extends ComponentException {

    private static final long serialVersionUID = 7142998120028361912L;

    public Authentication3DS2Exception(@NonNull String errorMessage) {
        super(errorMessage);
    }
}
