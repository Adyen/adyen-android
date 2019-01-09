/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 21/06/2018.
 */

package com.adyen.checkout.ui;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.handler.ErrorHandler;

public interface CheckoutSetupParametersHandler extends ErrorHandler {
    /**
     * Called when a payment session needs to be requested from the Adyen payments platform.
     *
     * @param checkoutSetupParameters The parameters that need to be submitted to Adyen in order to create the payment session.
     */
    void onRequestPaymentSession(@NonNull CheckoutSetupParameters checkoutSetupParameters);
}
