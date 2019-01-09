/*
 * Copyright (c) 2018 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by timon on 04/07/2018.
 */

package com.adyen.checkout.core.model;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.internal.ProvidedBy;
import com.adyen.checkout.core.internal.model.GooglePayConfigurationImpl;

@ProvidedBy(GooglePayConfigurationImpl.class)
public interface GooglePayConfiguration extends Configuration {
    /**
     * @return The current environment for the {@code WalletOptions}.
     */
    int getEnvironment();

    /**
     * @return The gateway for the {@code PaymentMethodTokenizationParameters}.
     */
    @NonNull
    String getGateway();

    /**
     * @return The gateway merchant ID for the {@code PaymentMethodTokenizationParameters}.
     */
    @NonNull
    String getGatewayMerchantId();
}
