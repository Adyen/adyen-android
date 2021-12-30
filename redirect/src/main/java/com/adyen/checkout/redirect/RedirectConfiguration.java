/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */

package com.adyen.checkout.redirect;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class RedirectConfiguration extends Configuration {

    public static final Parcelable.Creator<RedirectConfiguration> CREATOR = new Parcelable.Creator<RedirectConfiguration>() {
        public RedirectConfiguration createFromParcel(@NonNull Parcel in) {
            return new RedirectConfiguration(in);
        }

        public RedirectConfiguration[] newArray(int size) {
            return new RedirectConfiguration[size];
        }
    };

    protected RedirectConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
    }

    protected RedirectConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link RedirectConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<RedirectConfiguration> {

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String clientKey) {
            super(context, clientKey);
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull String clientKey) {
            super(shopperLocale, environment, clientKey);
        }

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        public Builder(@NonNull RedirectConfiguration configuration) {
            super(configuration);
        }

        @Override
        @NonNull
        public Builder setShopperLocale(@NonNull Locale builderShopperLocale) {
            return (Builder) super.setShopperLocale(builderShopperLocale);
        }

        @Override
        @NonNull
        public Builder setEnvironment(@NonNull Environment builderEnvironment) {
            return (Builder) super.setEnvironment(builderEnvironment);
        }

        @NonNull
        @Override
        protected RedirectConfiguration buildInternal() {
            return new RedirectConfiguration(this);
        }
    }
}

