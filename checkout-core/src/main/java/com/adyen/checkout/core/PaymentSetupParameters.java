/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/06/2018.
 */

package com.adyen.checkout.core;

import android.support.annotation.NonNull;

/**
 * The {@link PaymentSetupParameters} interface describes all relevant parameters to setup a payment on the Adyen payments platform.
 */
public interface PaymentSetupParameters {
    /**
     * @return The SDK token that needs to be submitted to Adyen in order to create the payment session.
     */
    @NonNull
    String getSdkToken();
}
