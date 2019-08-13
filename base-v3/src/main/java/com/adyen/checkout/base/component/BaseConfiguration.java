/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/7/2019.
 */

package com.adyen.checkout.base.component;

import android.support.annotation.NonNull;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public abstract class BaseConfiguration implements Configuration {

    private final Locale mShopperLocale;
    private final Environment mEnvironment;

    protected BaseConfiguration(@NonNull Locale shopperLocale, @NonNull Environment environment) {
        mShopperLocale = shopperLocale;
        mEnvironment = environment;
    }

    @NonNull
    public Environment getEnvironment() {
        return mEnvironment;
    }

    @NonNull
    @Override
    public Locale getShopperLocale() {
        return mShopperLocale;
    }
}
