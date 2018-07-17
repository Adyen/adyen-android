package com.adyen.checkout.core;

import android.support.annotation.NonNull;

/**
 * The {@link PaymentSetupParameters} interface describes all relevant parameters to setup a payment on the Adyen payments platform.
 * <p>
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 04/06/2018.
 */
public interface PaymentSetupParameters {
    /**
     * @return The SDK token that needs to be submitted to Adyen in order to create the payment session.
     */
    @NonNull
    String getSdkToken();
}
