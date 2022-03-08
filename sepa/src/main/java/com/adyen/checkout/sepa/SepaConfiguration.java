/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 22/8/2019.
 */

package com.adyen.checkout.sepa;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class SepaConfiguration extends Configuration {

    public static final Parcelable.Creator<SepaConfiguration> CREATOR = new Parcelable.Creator<SepaConfiguration>() {
        public SepaConfiguration createFromParcel(@NonNull Parcel in) {
            return new SepaConfiguration(in);
        }

        public SepaConfiguration[] newArray(int size) {
            return new SepaConfiguration[size];
        }
    };

    SepaConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
    }

    SepaConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link SepaConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<SepaConfiguration> {

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
        public Builder(@NonNull SepaConfiguration configuration) {
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
        protected SepaConfiguration buildInternal() {
            return new SepaConfiguration(this);
        }
    }
}
