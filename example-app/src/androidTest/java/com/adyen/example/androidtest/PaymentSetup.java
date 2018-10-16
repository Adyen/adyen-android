package com.adyen.example.androidtest;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Copyright (c) 2018 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by Ran Haveshush on 10/10/2018.
 */
public final class PaymentSetup {
    private Amount mAmount;

    private String mShopperLocale;

    private String mCountryCode;

    private String mReference;

    private String mShopperReference;

    private String mShopperEmail;

    private Configuration mConfiguration;

    public Amount getAmount() {
        return mAmount;
    }

    public String getShopperLocale() {
        return mShopperLocale;
    }

    public String getCountryCode() {
        return mCountryCode;
    }

    public String getReference() {
        return mReference;
    }

    public String getShopperReference() {
        return mShopperReference;
    }

    public String getShopperEmail() {
        return mShopperEmail;
    }

    public Configuration getConfiguration() {
        return mConfiguration;
    }

    public static final class Builder {
        private final PaymentSetup mPaymentSetup;

        public Builder() {
            mPaymentSetup = new PaymentSetup();
        }

        public Builder setAmount(@NonNull Amount amount) {
            mPaymentSetup.mAmount = amount;

            return this;
        }

        @NonNull
        public Builder setShopperLocale(@NonNull String shopperLocale) {
            mPaymentSetup.mShopperLocale = shopperLocale;

            return this;
        }

        @NonNull
        public Builder setCountryCode(@NonNull String countryCode) {
            mPaymentSetup.mCountryCode = countryCode;

            return this;
        }

        @NonNull
        public Builder setReference(@NonNull String reference) {
            mPaymentSetup.mReference = reference;

            return this;
        }

        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            mPaymentSetup.mShopperReference = shopperReference;

            return this;
        }

        @NonNull
        public Builder setShopperEmail(@Nullable String shopperEmail) {
            mPaymentSetup.mShopperEmail = shopperEmail;

            return this;
        }

        @NonNull
        public Builder setConfiguration(@Nullable Configuration configuration) {
            mPaymentSetup.mConfiguration = configuration;

            return this;
        }

        @NonNull
        public PaymentSetup build() {
            return mPaymentSetup;
        }
    }

    public static final class Amount {
        private Long mValue;

        private String mCurrency;

        public Amount(@NonNull Long value, @NonNull String currency) {
            mValue = value;
            mCurrency = currency;
        }

        @NonNull
        public Long getValue() {
            return mValue;
        }

        @NonNull
        public String getCurrency() {
            return mCurrency;
        }
    }

    public static final class Configuration {
        private Installments mInstallments;

        private CardHolderNameRequirement mCardHolderName;

        @Nullable
        public CardHolderNameRequirement getCardHolderName() {
            return mCardHolderName;
        }

        @Nullable
        public Installments getInstallments() {
            return mInstallments;
        }

        public void setCardHolderName(@Nullable CardHolderNameRequirement cardHolderName) {
            mCardHolderName = cardHolderName;
        }

        public void setInstallments(@Nullable Installments installments) {
            mInstallments = installments;
        }
    }

    public enum CardHolderNameRequirement {
        NONE,
        OPTIONAL,
        REQUIRED
    }

    public static final class Installments {
        private Integer mMaxNumberOfInstallments;

        public Installments(@NonNull Integer maxNumberOfInstallments) {
            mMaxNumberOfInstallments = maxNumberOfInstallments;
        }

        @NonNull
        public Integer getMaxNumberOfInstallments() {
            return mMaxNumberOfInstallments;
        }
    }
}

