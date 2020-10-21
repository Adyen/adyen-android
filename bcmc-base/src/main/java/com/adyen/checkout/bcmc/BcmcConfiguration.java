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
import androidx.annotation.Nullable;

import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.util.ValidationUtils;
import com.adyen.checkout.card.CardValidationUtils;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.CheckoutException;

import java.util.Locale;

/**
 * {@link Configuration} class required by {@link BcmcComponent} to change it's behavior. Pass it to the {@link BcmcComponent#PROVIDER}.
 */
public class BcmcConfiguration extends Configuration {

    private final String mPublicKey;

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
            @Nullable String clientKey,
            @NonNull String publicKey) {
        super(shopperLocale, environment, clientKey);

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
     * Get public key.
     *
     * @return {@link String}
     */
    @NonNull
    public String getPublicKey() {
        return mPublicKey;
    }

    /**
     * Builder to create a {@link BcmcConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<BcmcConfiguration> {

        private String mBuilderPublicKey;

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         */
        public Builder(@NonNull Context context) {
            super(context);
        }

        /**
         * Builder with required parameters for a {@link BcmcConfiguration}.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment) {
            super(shopperLocale, environment);
        }

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         * @deprecated      Constructor deprecated since publicKey is no longer always required in favor of clientKey.
         */
        @Deprecated
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
         * @deprecated          Constructor deprecated since publicKey is no longer always required in favor of clientKey.
         */
        @Deprecated
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderPublicKey = publicKey;
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

        /**
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        @NonNull
        public Builder setPublicKey(@NonNull String publicKey) {
            mBuilderPublicKey = publicKey;
            return this;
        }

        /**
         * Build {@link BcmcConfiguration} object from {@link BcmcConfiguration.Builder} inputs.
         *
         * @return {@link BcmcConfiguration}
         */
        @NonNull
        public BcmcConfiguration build() {

            if (!CardValidationUtils.isPublicKeyValid(mBuilderPublicKey)) {
                throw new CheckoutException("Invalid Public Key. Please find the valid public key on the Customer Area.");
            }

            // This will not be triggered until the public key check above is removed as it takes prioriy.
            if (!CardValidationUtils.isPublicKeyValid(mBuilderPublicKey) && !ValidationUtils.isClientKeyValid(mBuilderClientKey)) {
                throw new CheckoutException("You need either a valid Client key or Public key to use the Card Component.");
            }

            return new BcmcConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderClientKey,
                    mBuilderPublicKey
            );
        }
    }

}
