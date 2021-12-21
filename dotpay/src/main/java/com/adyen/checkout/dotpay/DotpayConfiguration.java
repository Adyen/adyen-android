/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.dotpay;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.BuildableConfiguration;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.issuerlist.IssuerListConfiguration;

import java.util.Locale;

public class DotpayConfiguration extends IssuerListConfiguration implements BuildableConfiguration<DotpayConfiguration> {

    public static final Parcelable.Creator<DotpayConfiguration> CREATOR = new Parcelable.Creator<DotpayConfiguration>() {
        public DotpayConfiguration createFromParcel(Parcel in) {
            return new DotpayConfiguration(in);
        }

        public DotpayConfiguration[] newArray(int size) {
            return new DotpayConfiguration[size];
        }
    };

    /**
     * @param builder The Builder instance to create the configuration.
     */
    DotpayConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
    }

    DotpayConfiguration(@NonNull Parcel in) {
        super(in);
    }

    @NonNull
    @Override
    public BaseConfigurationBuilder<DotpayConfiguration> toBuilder() {
        return new Builder(this);
    }

    /**
     * Builder to create a {@link DotpayConfiguration}.
     */
    public static final class Builder extends IssuerListBuilder<DotpayConfiguration> {

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
        public Builder(@NonNull DotpayConfiguration configuration) {
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
        protected DotpayConfiguration buildInternal() {
            return new DotpayConfiguration(this);
        }
    }
}
