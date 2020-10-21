/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 20/5/2019.
 */

package com.adyen.checkout.base;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.exception.CheckoutException;

/**
 * Data about an error that happened inside a component.
 */
public class ComponentError {

    private final CheckoutException mException;

    public ComponentError(@NonNull CheckoutException e) {
        mException = e;
    }

    /**
     * This message is not intended for user feedback, but for development feedback on what happened.
     *
     * @return A development driven error message from the Exception.
     */
    @NonNull
    public String getErrorMessage() {
        return mException.getMessage();
    }

    /**
     * Can be used to try to identify the root cause of the issue.
     *
     * @return The exception that happened.
     */
    @NonNull
    public CheckoutException getException() {
        return mException;
    }
}
