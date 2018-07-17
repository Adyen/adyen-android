package com.adyen.checkout.ui;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.PaymentSetupParameters;

/**
 * The {@link CheckoutSetupParameters} interface describes all relevant parameters to setup a payment on the Adyen payments platform in the context
 * of the quick integration.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 21/06/2018.
 */
public interface CheckoutSetupParameters extends PaymentSetupParameters {
    /**
     * @return The return URL that needs to be submitted to Adyen in order to create the payment session.
     */
    @NonNull
    String getReturnUrl();
}
