/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 7/3/2019.
 */

package com.adyen.checkout.base;

import android.support.annotation.NonNull;

import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

/**
 * Configuration class with extra information the merchant can or should provide to change the {@link PaymentComponent} behavior.
 */
public interface Configuration {

    @NonNull
    Locale getShopperLocale();

    @NonNull
    Environment getEnvironment();
}
