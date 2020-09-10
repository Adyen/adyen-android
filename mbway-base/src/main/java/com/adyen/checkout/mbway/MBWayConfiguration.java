/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 11/8/2020.
 */

package com.adyen.checkout.mbway;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

@SuppressWarnings("AbbreviationAsWordInName")
public class MBWayConfiguration extends Configuration {

    public static final Parcelable.Creator<MBWayConfiguration> CREATOR = new Parcelable.Creator<MBWayConfiguration>() {
        public MBWayConfiguration createFromParcel(@NonNull Parcel in) {
            return new MBWayConfiguration(in);
        }

        public MBWayConfiguration[] newArray(int size) {
            return new MBWayConfiguration[size];
        }
    };

    MBWayConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey
    ) {
        super(shopperLocale, environment, clientKey);
    }

    MBWayConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link MBWayConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<MBWayConfiguration> {

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
        public MBWayConfiguration build() {
            return new MBWayConfiguration(mBuilderShopperLocale, mBuilderEnvironment, mBuilderClientKey);
        }
    }
}
