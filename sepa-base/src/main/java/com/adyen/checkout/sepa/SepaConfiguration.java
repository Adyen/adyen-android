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

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.code.Lint;

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

    @SuppressWarnings(Lint.SYNTHETIC)
    SepaConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey
    ) {
        super(shopperLocale, environment, clientKey);
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
         */
        public Builder(@NonNull Context context) {
            super(context);
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment) {
            super(shopperLocale, environment);
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
        public Builder setClientKey(@NonNull String builderClientKey) {
            return (Builder) super.setClientKey(builderClientKey);
        }

        @NonNull
        @Override
        public SepaConfiguration build() {
            return new SepaConfiguration(mBuilderShopperLocale, mBuilderEnvironment, mBuilderClientKey);
        }
    }
}
