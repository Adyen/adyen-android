/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class IssuerListConfiguration implements Configuration {

    private final DisplayMetrics mDisplayMetrics;
    private final Environment mEnvironment;
    private final Locale mShopperLocale;

    /**
     * Constructor with minimal required parameters for this Configuration.
     *
     * @param shopperLocale  The locale that should be used to display strings and layouts. Can differ from device default.
     * @param displayMetrics The current {@link DisplayMetrics} of the device to fetch images of matching size.
     * @param environment    The environment to be used to make network calls.
     */
    public IssuerListConfiguration(@NonNull Locale shopperLocale, @NonNull DisplayMetrics displayMetrics, @NonNull Environment environment) {
        mShopperLocale = shopperLocale;
        mDisplayMetrics = displayMetrics;
        mEnvironment = environment;
    }

    @NonNull
    DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
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
