/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */

package com.adyen.checkout.issuerlist;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public abstract class IssuerListConfiguration extends Configuration {

    protected IssuerListConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey
    ) {
        super(shopperLocale, environment, clientKey);
    }

    protected IssuerListConfiguration(@NonNull Parcel in) {
        super(in);
    }


    public abstract static class IssuerListBuilder<IssuerListConfigurationT extends IssuerListConfiguration>
            extends BaseConfigurationBuilder<IssuerListConfigurationT> {

        protected IssuerListBuilder(@NonNull Context context) {
            super(context);
        }

        protected IssuerListBuilder(@NonNull Locale shopperLocale, @NonNull Environment environment) {
            super(shopperLocale, environment);
        }
    }
}
