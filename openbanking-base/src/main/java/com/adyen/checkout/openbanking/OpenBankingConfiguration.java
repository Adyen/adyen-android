/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */

package com.adyen.checkout.openbanking;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.issuerlist.IssuerListConfiguration;

import java.util.Locale;

public class OpenBankingConfiguration extends IssuerListConfiguration {

    public static final Parcelable.Creator<OpenBankingConfiguration> CREATOR = new Parcelable.Creator<OpenBankingConfiguration>() {
        public OpenBankingConfiguration createFromParcel(@NonNull Parcel in) {
            return new OpenBankingConfiguration(in);
        }

        public OpenBankingConfiguration[] newArray(int size) {
            return new OpenBankingConfiguration[size];
        }
    };

    /**
     * @param shopperLocale  The locale that should be used to display strings and layouts. Can differ from device default.
     * @param displayMetrics The current {@link DisplayMetrics} of the device to fetch images of matching size.
     * @param environment    The environment to be used to make network calls.
     * @deprecated Constructor with all parameters. Use the Builder to initialize this object.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public OpenBankingConfiguration(
            @NonNull Locale shopperLocale,
            @SuppressWarnings("PMD.UnusedFormalParameter")
            @Nullable DisplayMetrics displayMetrics,
            @NonNull Environment environment
    ) {
        super(shopperLocale, environment);
    }

    /**
     * @param shopperLocale The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment   The environment to be used to make network calls.
     */
    OpenBankingConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment
    ) {
        super(shopperLocale, environment);
    }

    OpenBankingConfiguration(@NonNull Parcel in) {
        super(in);
    }

    /**
     * Builder to create a {@link OpenBankingConfiguration} more easily.
     */
    public static final class Builder extends IssuerListBuilder<OpenBankingConfiguration> {

        public Builder(@NonNull Context context) {
            super(context);
        }

        /**
         * @deprecated No need to pass {@link DisplayMetrics} to builder.
         */
        @SuppressWarnings("DeprecatedIsStillUsed")
        @Deprecated
        public Builder(@NonNull Locale shopperLocale,
                @SuppressWarnings("PMD.UnusedFormalParameter")
                @Nullable DisplayMetrics displayMetrics,
                @NonNull Environment environment) {
            super(shopperLocale, environment);
        }

        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment) {
            super(shopperLocale, environment);
        }

        @NonNull
        @Override
        public OpenBankingConfiguration build() {
            return new OpenBankingConfiguration(mBuilderShopperLocale, mBuilderEnvironment);
        }
    }
}
