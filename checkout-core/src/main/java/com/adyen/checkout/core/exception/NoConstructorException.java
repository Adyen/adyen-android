/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 17/12/2020.
 */

package com.adyen.checkout.core.exception;

/**
 * Exception to prevent instantiation of utility classes.
 */
public class NoConstructorException extends IllegalStateException {
    private static final long serialVersionUID = -5460575792365783947L;

    public NoConstructorException() {
        super("No instances allowed.");
    }
}
