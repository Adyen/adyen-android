/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/7/2019.
 */

package com.adyen.checkout.base.component;

import android.content.Context;
import android.support.annotation.NonNull;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.util.LocaleUtil;

import java.util.Locale;

public abstract class BaseConfigurationBuilder<ConfigurationT extends Configuration> {

    @NonNull
    protected Locale mBuilderShopperLocale;

    @NonNull
    protected Environment mBuilderEnvironment;

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     */
    public BaseConfigurationBuilder(@NonNull Context context) {
        this(LocaleUtil.getLocale(context), Environment.TEST);
    }

    /**
     * Base constructor with the required fields.
     *
     * @param shopperLocale The Locale of the shopper.
     * @param environment   The {@link Environment} to be used for network calls to Adyen.
     */
    public BaseConfigurationBuilder(@NonNull Locale shopperLocale, @NonNull Environment environment) {
        mBuilderShopperLocale = shopperLocale;
        mBuilderEnvironment = environment;
    }

    @NonNull
    public BaseConfigurationBuilder setShopperLocale(@NonNull Locale builderShopperLocale) {
        mBuilderShopperLocale = builderShopperLocale;
        return this;
    }

    @NonNull
    public BaseConfigurationBuilder setEnvironment(@NonNull Environment builderEnvironment) {
        mBuilderEnvironment = builderEnvironment;
        return this;
    }

    @NonNull
    public abstract ConfigurationT build();
}
