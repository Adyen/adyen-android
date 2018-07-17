package com.adyen.checkout.core.handler;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.CheckoutException;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 13/07/2018.
 */
public interface ErrorHandler {
    /**
     * Called when an error occurred during the payment.
     *
     * @param error The error that occurred.
     */
    void onError(@NonNull CheckoutException error);
}
