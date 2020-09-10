/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 7/11/2019.
 */

package com.adyen.checkout.afterpay;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.code.Lint;

import java.util.Locale;

public class AfterPayConfiguration extends Configuration {

    public enum VisibilityState {
        EDITABLE,
        READ_ONLY,
        HIDDEN
    }

    public enum CountryCode {
        NL(new Locale("", "nl")),
        BE(new Locale("", "be"));

        private Locale mLocale;

        CountryCode(Locale locale) {
            this.mLocale = locale;
        }

        @NonNull
        public Locale getLocale() {
            return mLocale;
        }
    }

    private final VisibilityState mPersonalDetailsVisibility;
    private final VisibilityState mBillingAddressVisibility;
    private final VisibilityState mDeliveryAddressVisibility;
    private final CountryCode mCountryCode;

    public static final Parcelable.Creator<AfterPayConfiguration> CREATOR = new Parcelable.Creator<AfterPayConfiguration>() {
        public AfterPayConfiguration createFromParcel(@NonNull Parcel in) {
            return new AfterPayConfiguration(in);
        }

        public AfterPayConfiguration[] newArray(int size) {
            return new AfterPayConfiguration[size];
        }
    };

    @SuppressWarnings(Lint.SYNTHETIC)
    AfterPayConfiguration(@NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String clientKey,
            @NonNull VisibilityState builderPersonalDetailsVisibility,
            @NonNull VisibilityState billingAddressVisibility,
            @NonNull VisibilityState deliveryAddressVisibility,
            @NonNull CountryCode countryCode) {
        super(shopperLocale, environment, clientKey);
        this.mPersonalDetailsVisibility = builderPersonalDetailsVisibility;
        this.mBillingAddressVisibility = billingAddressVisibility;
        this.mDeliveryAddressVisibility = deliveryAddressVisibility;
        this.mCountryCode = countryCode;
    }

    AfterPayConfiguration(@NonNull Parcel in) {
        super(in);
        this.mPersonalDetailsVisibility = (VisibilityState) in.readSerializable();
        this.mBillingAddressVisibility = (VisibilityState) in.readSerializable();
        this.mDeliveryAddressVisibility = (VisibilityState) in.readSerializable();
        this.mCountryCode = (CountryCode) in.readSerializable();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeSerializable(mPersonalDetailsVisibility);
        dest.writeSerializable(mBillingAddressVisibility);
        dest.writeSerializable(mDeliveryAddressVisibility);
        dest.writeSerializable(mCountryCode);
    }

    @NonNull
    public VisibilityState getPersonalDetailsVisibility() {
        return mPersonalDetailsVisibility;
    }

    @NonNull
    public VisibilityState getBillingAddressVisibility() {
        return mBillingAddressVisibility;
    }

    @NonNull
    public VisibilityState getDeliveryAddressVisibility() {
        return mDeliveryAddressVisibility;
    }

    @NonNull
    public CountryCode getCountryCode() {
        return mCountryCode;
    }

    /**
     * Builder to create a {@link AfterPayConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<AfterPayConfiguration> {

        private VisibilityState mBuilderPersonalDetailsState = VisibilityState.EDITABLE;
        private VisibilityState mBuilderBillingAddressState = VisibilityState.EDITABLE;
        private VisibilityState mBuilderDeliveryAddressState = VisibilityState.EDITABLE;
        private CountryCode mCountry;

        /**
         * Constructor for Builder with default values.
         *
         * @param context A context
         */
        public Builder(@NonNull Context context, @NonNull CountryCode country) {
            super(context);
            this.mCountry = country;
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull CountryCode country) {
            super(shopperLocale, environment);
            this.mCountry = country;
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
        public Builder setPersonalDetailsState(@NonNull VisibilityState visibilityState) {
            this.mBuilderPersonalDetailsState = visibilityState;
            return this;
        }

        @NonNull
        public VisibilityState getPersonalDetailsState() {
            return mBuilderPersonalDetailsState;
        }

        @NonNull
        public Builder setBillingAddressState(@NonNull VisibilityState visibilityState) {
            this.mBuilderBillingAddressState = visibilityState;
            return this;
        }

        @NonNull
        public VisibilityState getBillingAddressState() {
            return mBuilderBillingAddressState;
        }

        @NonNull
        public Builder setDeliveryAddressState(@NonNull VisibilityState visibilityState) {
            this.mBuilderDeliveryAddressState = visibilityState;
            return this;
        }

        @NonNull
        public Builder setCountry(@NonNull CountryCode country) {
            mCountry = country;
            return this;
        }

        @NonNull
        public CountryCode getCountry() {
            return mCountry;
        }

        @NonNull
        public VisibilityState getDeliveryAddressState() {
            return mBuilderDeliveryAddressState;
        }

        @NonNull
        @Override
        public AfterPayConfiguration build() {
            return new AfterPayConfiguration(mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderClientKey,
                    mBuilderPersonalDetailsState,
                    mBuilderBillingAddressState,
                    mBuilderDeliveryAddressState,
                    mCountry);
        }
    }
}
