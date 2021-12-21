/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */

package com.adyen.checkout.blik;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class BlikConfiguration extends Configuration {

    public static final Parcelable.Creator<BlikConfiguration> CREATOR = new Parcelable.Creator<BlikConfiguration>() {
        public BlikConfiguration createFromParcel(@NonNull Parcel in) {
            return new BlikConfiguration(in);
        }

        public BlikConfiguration[] newArray(int size) {
            return new BlikConfiguration[size];
        }
    };

    BlikConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
    }

    BlikConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link BlikConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<BlikConfiguration> {

        /**
         * Constructor for Builder with default values.
         *
         * @param context A context
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
        public Builder(@NonNull BlikConfiguration configuration) {
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
        protected BlikConfiguration buildInternal() {
            return new BlikConfiguration(this);
        }
    }
}
