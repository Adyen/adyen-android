/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.eps;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.issuerlist.IssuerListConfiguration;

import java.util.Locale;

@SuppressWarnings("AbbreviationAsWordInName")
public class EPSConfiguration extends IssuerListConfiguration {

    public static final Parcelable.Creator<EPSConfiguration> CREATOR = new Parcelable.Creator<EPSConfiguration>() {
        public EPSConfiguration createFromParcel(@NonNull Parcel in) {
            return new EPSConfiguration(in);
        }

        public EPSConfiguration[] newArray(int size) {
            return new EPSConfiguration[size];
        }
    };

    /**
     * @param builder The Builder instance to create the configuration.
     */
    EPSConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
    }

    EPSConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link EPSConfiguration}.
     */
    public static final class Builder extends IssuerListBuilder<EPSConfiguration> {

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
        public Builder(@NonNull EPSConfiguration configuration) {
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
        public EPSConfiguration buildInternal() {
            return new EPSConfiguration(this);
        }
    }
}
