/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.eps;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.issuerlist.IssuerListConfiguration;

import java.util.Locale;

@SuppressWarnings("AbbreviationAsWordInName")
public class EPSConfiguration extends IssuerListConfiguration {

    /**
     * Constructor with all parameters. You can use the Builder to initialize this object more easily.
     *
     * @param shopperLocale  The locale that should be used to display strings and layouts. Can differ from device default.
     * @param displayMetrics The current {@link DisplayMetrics} of the device to fetch images of matching size.
     * @param environment    The environment to be used to make network calls.
     */
    public EPSConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull DisplayMetrics displayMetrics,
            @NonNull Environment environment
    ) {
        super(shopperLocale, displayMetrics, environment);
    }

    /**
     * Builder to create a {@link EPSConfiguration} more easily.
     */
    public static final class Builder extends IssuerListBuilder<EPSConfiguration> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull DisplayMetrics displayMetrics) {
            super(shopperLocale, environment, displayMetrics);
        }

        @NonNull
        @Override
        public EPSConfiguration build() {
            return new EPSConfiguration(mBuilderShopperLocale, mBuilderDisplayMetrics, mBuilderEnvironment);
        }
    }
}
