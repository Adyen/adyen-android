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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.component.BaseConfiguration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.core.api.Environment;

import java.util.Locale;

/**
 * {@link Configuration} class required by {@link BcmcComponent} to change it's behavior. Pass it to the {@link BcmcComponent#PROVIDER}.
 */
public class BcmcConfiguration extends BaseConfiguration {

    private final String mPublicKey;

    public static final Parcelable.Creator<BcmcConfiguration> CREATOR = new Parcelable.Creator<BcmcConfiguration>() {
        public BcmcConfiguration createFromParcel(@NonNull Parcel in) {
            return new BcmcConfiguration(in);
        }

        public BcmcConfiguration[] newArray(int size) {
            return new BcmcConfiguration[size];
        }
    };


    /**
     * @param shopperLocale The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment   The environment to be used to make network calls.
     * @param publicKey     The public key used for encryption of the card data. You can get it from the Customer Area.
     */
    BcmcConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String publicKey) {
        super(shopperLocale, environment);

        mPublicKey = publicKey;
    }

    BcmcConfiguration(@NonNull Parcel in) {
        super(in);
        mPublicKey = in.readString();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mPublicKey);
    }

    /**
     * Get display metrics.
     *
     * @return {@link DisplayMetrics}
     * @deprecated There is no need for {@link DisplayMetrics} in builder any more, it'll always return null
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @Nullable
    public DisplayMetrics getDisplayMetrics() {
        return null;
    }

    /**
     * Get public key.
     *
     * @return {@link String}
     */
    @NonNull
    public String getPublicKey() {
        return mPublicKey;
    }

    /**
     * Builder to create a {@link BcmcConfiguration} more easily.
     */
    public static final class Builder extends BaseConfigurationBuilder<BcmcConfiguration> {

        private String mBuilderPublicKey;

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public Builder(@NonNull Context context, @NonNull String publicKey) {
            super(context);
            mBuilderPublicKey = publicKey;
        }

        /**
         * Builder with required parameters for a {@link BcmcConfiguration}.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         * @param publicKey     The public key used for encryption of the card data. You can get it from the Customer Area.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderPublicKey = publicKey;
        }

        /**
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public void setPublicKey(@NonNull String publicKey) {
            mBuilderPublicKey = publicKey;
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
                    mBuilderPublicKey
            );
        }
    }

}
