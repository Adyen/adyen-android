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

import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.util.ParcelUtils;

import java.util.Locale;

/**
 * {@link Configuration} class required by {@link BcmcComponent} to change it's behavior. Pass it to the {@link BcmcComponent#PROVIDER}.
 */
public class BcmcConfiguration extends Configuration {

    private final String mShopperReference;
    private final boolean mShowStorePaymentField;

    public static final Parcelable.Creator<BcmcConfiguration> CREATOR = new Parcelable.Creator<BcmcConfiguration>() {
        public BcmcConfiguration createFromParcel(@NonNull Parcel in) {
            return new BcmcConfiguration(in);
        }

        public BcmcConfiguration[] newArray(int size) {
            return new BcmcConfiguration[size];
        }
    };

    BcmcConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());

        mShopperReference = builder.mShopperReference;
        mShowStorePaymentField = builder.mBuilderShowStorePaymentField;
    }

    BcmcConfiguration(@NonNull Parcel in) {
        super(in);
        mShopperReference = in.readString();
        mShowStorePaymentField = ParcelUtils.readBoolean(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mShopperReference);
        ParcelUtils.writeBoolean(dest, mShowStorePaymentField);
    }

    @Nullable
    public String getShopperReference() {
        return mShopperReference;
    }

    public boolean isStorePaymentFieldVisible() {
        return mShowStorePaymentField;
    }

    /**
     * Builder to create a {@link BcmcConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<BcmcConfiguration> {

        private boolean mBuilderShowStorePaymentField = false;
        private String mShopperReference;

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

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        public Builder(@NonNull BcmcConfiguration configuration) {
            super(configuration);
            mShopperReference = configuration.getShopperReference();
            mBuilderShowStorePaymentField = configuration.isStorePaymentFieldVisible();
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
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * @param showStorePaymentField {@link Boolean}
         * @return {@link BcmcConfiguration.Builder}
         */
        @NonNull
        public BcmcConfiguration.Builder setShowStorePaymentField(boolean showStorePaymentField) {
            mBuilderShowStorePaymentField = showStorePaymentField;
            return this;
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the {@link com.adyen.checkout.components.model.payments.request.PaymentComponentData}
         * for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return {@link BcmcConfiguration.Builder}
         */
        @NonNull
        public BcmcConfiguration.Builder setShopperReference(@NonNull String shopperReference) {
            mShopperReference = shopperReference;
            return this;
        }

        /**
         * Build {@link BcmcConfiguration} object from {@link BcmcConfiguration.Builder} inputs.
         *
         * @return {@link BcmcConfiguration}
         */
        @NonNull
        protected BcmcConfiguration buildInternal() {
            return new BcmcConfiguration(this);
        }
    }

}
