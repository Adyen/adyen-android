/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */

package com.adyen.checkout.bcmc;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

/**
 * {@link Configuration} class required by {@link BcmcComponent} to change it's behavior. Pass it to the {@link BcmcComponent#PROVIDER}.
 */
public class BcmcConfiguration extends Configuration {

    public static final Parcelable.Creator<BcmcConfiguration> CREATOR = new Parcelable.Creator<BcmcConfiguration>() {
        public BcmcConfiguration createFromParcel(@NonNull Parcel in) {
            return new BcmcConfiguration(in);
        }

        public BcmcConfiguration[] newArray(int size) {
            return new BcmcConfiguration[size];
        }
    };

    BcmcConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey) {
        super(shopperLocale, environment, clientKey);
    }

    BcmcConfiguration(@NonNull Parcel in) {
        super(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    /**
     * Builder to create a {@link BcmcConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<BcmcConfiguration> {

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String clientKey) {
            super(context, clientKey);
        }

        /**
         * Builder with required parameters for a {@link BcmcConfiguration}.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull String clientKey
        ) {
            super(shopperLocale, environment, clientKey);
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

        /**
         * Build {@link BcmcConfiguration} object from {@link BcmcConfiguration.Builder} inputs.
         *
         * @return {@link BcmcConfiguration}
         */
        @NonNull
        public BcmcConfiguration build() {
            return new BcmcConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderClientKey
            );
        }
    }

}
