/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */

package com.adyen.checkout.adyen3ds2;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

public class Adyen3DS2Configuration extends Configuration {

    public static final Parcelable.Creator<Adyen3DS2Configuration> CREATOR = new Parcelable.Creator<Adyen3DS2Configuration>() {
        public Adyen3DS2Configuration createFromParcel(@NonNull Parcel in) {
            return new Adyen3DS2Configuration(in);
        }

        public Adyen3DS2Configuration[] newArray(int size) {
            return new Adyen3DS2Configuration[size];
        }
    };

    protected Adyen3DS2Configuration(@NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @Nullable String clientKey) {
        super(shopperLocale, environment, clientKey);
    }

    protected Adyen3DS2Configuration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link Adyen3DS2Configuration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<Adyen3DS2Configuration> {

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
        public Adyen3DS2Configuration build() {
            return new Adyen3DS2Configuration(mBuilderShopperLocale, mBuilderEnvironment, mBuilderClientKey);
        }
    }
}
