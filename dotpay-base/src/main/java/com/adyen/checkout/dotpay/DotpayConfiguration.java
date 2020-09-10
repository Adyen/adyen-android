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

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.issuerlist.IssuerListConfiguration;

import java.util.Locale;

public class DotpayConfiguration extends IssuerListConfiguration {

    public static final Parcelable.Creator<DotpayConfiguration> CREATOR = new Parcelable.Creator<DotpayConfiguration>() {
        public DotpayConfiguration createFromParcel(Parcel in) {
            return new DotpayConfiguration(in);
        }

        public DotpayConfiguration[] newArray(int size) {
            return new DotpayConfiguration[size];
        }
    };

    /**
     * @param shopperLocale The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment   The environment to be used to make network calls.
     */
    DotpayConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey
    ) {
        super(shopperLocale, environment, clientKey);
    }

    DotpayConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link DotpayConfiguration}.
     */
    public static final class Builder extends IssuerListBuilder<DotpayConfiguration> {

        public Builder(@NonNull Context context) {
            super(context);
        }

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
        public DotpayConfiguration build() {
            return new DotpayConfiguration(mBuilderShopperLocale, mBuilderEnvironment, mBuilderClientKey);
        }
    }
}
