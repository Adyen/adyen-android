package com.adyen.example.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import java.io.Serializable;

/**
 * Copyright (c) 2017 Adyen B.V.
 * <p>
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 * <p>
 * Created by timon on 08/08/2017.
 */
public final class PaymentSetupRequest implements Serializable {
    @Json(name = "merchantAccount")
    private String mMerchantAccount;

    @Json(name = "shopperLocale")
    private String mShopperLocale;

    @Json(name = "token")
    private String mToken;

    @Json(name = "returnUrl")
    private String mReturnUrl;

    @Json(name = "countryCode")
    private String mCountryCode;

    @Json(name = "amount")
    private Amount mAmount;

    @Json(name = "channel")
    private String mChannel = "Android";

    @Json(name = "reference")
    private String mReference;

    @Json(name = "shopperReference")
    private String mShopperReference;

    @Json(name = "shopperEmail")
    private String mShopperEmail;

    @Json(name = "configuration")
    private Configuration mConfiguration;

    public static final class Builder {
        private final PaymentSetupRequest mPaymentSetupRequest;

        public Builder(@NonNull String merchantAccount, @NonNull String token, @NonNull String returnUrl, @NonNull Amount amount) {
            mPaymentSetupRequest = new PaymentSetupRequest();
            mPaymentSetupRequest.mMerchantAccount = merchantAccount;
            mPaymentSetupRequest.mToken = token;
            mPaymentSetupRequest.mReturnUrl = returnUrl;
            mPaymentSetupRequest.mAmount = amount;
        }

        @NonNull
        public Builder setShopperLocale(@NonNull String shopperLocale) {
            mPaymentSetupRequest.mShopperLocale = shopperLocale;

            return this;
        }

        @NonNull
        public Builder setCountryCode(@NonNull String countryCode) {
            mPaymentSetupRequest.mCountryCode = countryCode;

            return this;
        }

        @NonNull
        public Builder setReference(@NonNull String reference) {
            mPaymentSetupRequest.mReference = reference;

            return this;
        }

        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            mPaymentSetupRequest.mShopperReference = shopperReference;

            return this;
        }

        @NonNull
        public Builder setShopperEmail(@Nullable String shopperEmail) {
            mPaymentSetupRequest.mShopperEmail = shopperEmail;

            return this;
        }

        @NonNull
        public Builder setConfiguration(@Nullable Configuration configuration) {
            mPaymentSetupRequest.mConfiguration = configuration;

            return this;
        }

        @NonNull
        public PaymentSetupRequest build() {
            return mPaymentSetupRequest;
        }
    }

    public static final class Amount implements Serializable {
        @Json(name = "value")
        private Long mValue;

        @Json(name = "currency")
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

    public static final class Configuration implements Serializable {
        @Json(name = "installments")
        private Installments mInstallments;

        @Json(name = "cardHolderName")
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

    public static final class Installments implements Serializable {
        @Json(name = "maxNumberOfInstallments")
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
