/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 20/5/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.exeption.CheckoutException;

/**
 * Data about an error that happened inside a component.
 */
public class ComponentError {

    private String mErrorMessage;

    public ComponentError(@NonNull String errorMessage) {
        mErrorMessage = errorMessage;
    }

    public ComponentError(@NonNull CheckoutException e) {
        this(e.getMessage());
    }

    /**
     * The message about the error. This message is not intended for user feedback, but for development feedback on what happened.
     *
     * @return A development driven error message.
     */
    @NonNull
    public String getErrorMessage() {
        return mErrorMessage;
    }
}
