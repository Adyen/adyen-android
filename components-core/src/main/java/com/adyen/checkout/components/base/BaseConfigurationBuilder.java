/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/7/2019.
 */

package com.adyen.checkout.components.base;

import android.content.Context;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.util.ValidationUtils;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.LocaleUtil;

import java.util.Locale;

public abstract class BaseConfigurationBuilder<ConfigurationT extends Configuration> {

    @NonNull
    protected Locale mBuilderShopperLocale;

    @NonNull
    protected Environment mBuilderEnvironment;

    @NonNull
    protected String mBuilderClientKey;

    /**
     * Constructor that provides default values.
     *
     * @param context A Context
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
     */
    public BaseConfigurationBuilder(@NonNull Context context, @NonNull String clientKey) {
        this(LocaleUtil.getLocale(context), Environment.TEST, clientKey);
    }

    /**
     * Base constructor with the required fields.
     *
     * @param shopperLocale The Locale of the shopper.
     * @param environment   The {@link Environment} to be used for network calls to Adyen.
     * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
     */
    public BaseConfigurationBuilder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull String clientKey) {
        mBuilderShopperLocale = shopperLocale;
        mBuilderEnvironment = environment;

        if (!ValidationUtils.isClientKeyValid(clientKey)) {
            throw new CheckoutException("Client key is not valid.");
        }
        mBuilderClientKey = clientKey;
    }

    /**
     * @param builderShopperLocale the {@link Locale} used for translations.
     * @return The builder instance to chain calls.
     */
    @NonNull
    public BaseConfigurationBuilder<ConfigurationT> setShopperLocale(@NonNull Locale builderShopperLocale) {
        mBuilderShopperLocale = builderShopperLocale;
        return this;
    }

    /**
     * @param builderEnvironment The {@link Environment} used for network calls.
     * @return The builder instance to chain calls.
     */
    @NonNull
    public BaseConfigurationBuilder<ConfigurationT> setEnvironment(@NonNull Environment builderEnvironment) {
        mBuilderEnvironment = builderEnvironment;
        return this;
    }

    @NonNull
    public abstract ConfigurationT build();
}
