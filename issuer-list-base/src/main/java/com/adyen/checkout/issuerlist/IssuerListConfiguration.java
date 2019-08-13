/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.component.BaseConfiguration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public abstract class IssuerListConfiguration extends BaseConfiguration {

    private final DisplayMetrics mDisplayMetrics;

    protected IssuerListConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull DisplayMetrics displayMetrics,
            @NonNull Environment environment
    ) {
        super(shopperLocale, environment);
        mDisplayMetrics = displayMetrics;
    }

    @NonNull
    DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }


    public abstract static class IssuerListBuilder<IssuerListConfigurationT extends IssuerListConfiguration>
            extends BaseConfigurationBuilder<IssuerListConfigurationT> {

        @NonNull
        protected DisplayMetrics mBuilderDisplayMetrics;

        protected IssuerListBuilder(@NonNull Context context) {
            super(context);
            mBuilderDisplayMetrics = context.getResources().getDisplayMetrics();
        }

        protected IssuerListBuilder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull DisplayMetrics displayMetrics) {
            super(shopperLocale, environment);
            mBuilderDisplayMetrics = displayMetrics;
        }

        public void setDisplayMetrics(@NonNull DisplayMetrics builderDisplayMetrics) {
            mBuilderDisplayMetrics = builderDisplayMetrics;
        }
    }
}
