/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.adyen.checkout.base.component.BaseConfiguration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.model.payments.Amount;
import com.adyen.checkout.base.util.CheckoutCurrency;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exeption.CheckoutException;
import com.adyen.checkout.googlepay.model.BillingAddressParameters;
import com.adyen.checkout.googlepay.model.MerchantInfo;
import com.adyen.checkout.googlepay.model.ShippingAddressParameters;
import com.adyen.checkout.googlepay.util.AllowedAuthMethods;
import com.adyen.checkout.googlepay.util.AllowedCardNetworks;
import com.google.android.gms.wallet.WalletConstants;

import java.util.List;
import java.util.Locale;

public class GooglePayConfiguration extends BaseConfiguration {

    private final String mMerchantAccount;
    private final int mGooglePayEnvironment;
    private final Amount mAmount;
    private final MerchantInfo mMerchantInfo;
    private final List<String> mAllowedAuthMethods;
    private final List<String> mAllowedCardNetworks;
    private final boolean mAllowPrepaidCards;
    private final boolean mEmailRequired;
    private final boolean mExistingPaymentMethodRequired;
    private final boolean mShippingAddressRequired;
    private final ShippingAddressParameters mShippingAddressParameters;
    private final boolean mBillingAddressRequired;
    private final BillingAddressParameters mBillingAddressParameters;

    /**
     * Constructor with all parameters. You can use the Builder to initialize this object more easily.
     */
    @SuppressWarnings("ParameterNumber")
    public GooglePayConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String merchantAccount,
            int googlePayEnvironment,
            @NonNull Amount amount,
            @Nullable MerchantInfo merchantInfo,
            @NonNull List<String> allowedAuthMethods,
            @NonNull List<String> allowedCardNetworks,
            boolean allowPrepaidCards,
            boolean emailRequired,
            boolean existingPaymentMethodRequired,
            boolean shippingAddressRequired,
            @Nullable ShippingAddressParameters shippingAddressParameters,
            boolean billingAddressRequired,
            @Nullable BillingAddressParameters billingAddressParameters
    ) {
        super(shopperLocale, environment);
        mMerchantAccount = merchantAccount;
        mGooglePayEnvironment = googlePayEnvironment;
        mAmount = amount;
        mMerchantInfo = merchantInfo;
        mAllowedAuthMethods = allowedAuthMethods;
        mAllowedCardNetworks = allowedCardNetworks;
        mAllowPrepaidCards = allowPrepaidCards;
        mEmailRequired = emailRequired;
        mExistingPaymentMethodRequired = existingPaymentMethodRequired;
        mShippingAddressRequired = shippingAddressRequired;
        mShippingAddressParameters = shippingAddressParameters;
        mBillingAddressRequired = billingAddressRequired;
        mBillingAddressParameters = billingAddressParameters;
    }

    @NonNull
    public String getMerchantAccount() {
        return mMerchantAccount;
    }

    @NonNull
    public Amount getAmount() {
        return mAmount;
    }

    public int getGooglePayEnvironment() {
        return mGooglePayEnvironment;
    }

    @Nullable
    public MerchantInfo getMerchantInfo() {
        return mMerchantInfo;
    }

    @Nullable
    public List<String> getAllowedAuthMethods() {
        return mAllowedAuthMethods;
    }

    @Nullable
    public List<String> getAllowedCardNetworks() {
        return mAllowedCardNetworks;
    }

    public boolean isAllowPrepaidCards() {
        return mAllowPrepaidCards;
    }

    public boolean isEmailRequired() {
        return mEmailRequired;
    }

    public boolean isExistingPaymentMethodRequired() {
        return mExistingPaymentMethodRequired;
    }

    public boolean isShippingAddressRequired() {
        return mShippingAddressRequired;
    }

    @Nullable
    public ShippingAddressParameters getShippingAddressParameters() {
        return mShippingAddressParameters;
    }

    public boolean isBillingAddressRequired() {
        return mBillingAddressRequired;
    }

    @Nullable
    public BillingAddressParameters getBillingAddressParameters() {
        return mBillingAddressParameters;
    }

    /**
     * Google Pay Configuration Builder.
     */
    public static final class Builder extends BaseConfigurationBuilder<GooglePayConfiguration> {

        private String mBuilderMerchantAccount;
        private int mBuilderGooglePayEnvironment = WalletConstants.ENVIRONMENT_TEST;
        private Amount mBuilderAmount = createDefaultAmount();
        private MerchantInfo mBuilderMerchantInfo = null;
        private List<String> mBuilderAllowedAuthMethods = AllowedAuthMethods.getAllAllowedAuthMethods();
        private List<String> mBuilderAllowedCardNetworks = AllowedCardNetworks.getAllAllowedCardNetworks();
        private boolean mBuilderAllowPrepaidCards = false;
        private boolean mBuilderEmailRequired;
        private boolean mBuilderExistingPaymentMethodRequired;
        private boolean mBuilderShippingAddressRequired;
        private ShippingAddressParameters mBuilderShippingAddressParameters;
        private boolean mBuilderBillingAddressRequired;
        private BillingAddressParameters mBuilderBillingAddressParameters;

        private static Amount createDefaultAmount() {
            final Amount defaultAmount = new Amount();
            defaultAmount.setValue(0);
            defaultAmount.setCurrency(CheckoutCurrency.USD.name());
            return defaultAmount;
        }

        /**
         * Builder with required parameters.
         *
         * @param context A context to get some information.
         * @param merchantAccount Your merchant account with Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String merchantAccount) {
            super(context);
            mBuilderMerchantAccount = merchantAccount;
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The locale of the Shopper for translation.
         * @param environment TThe {@link Environment} to be used for network calls to Adyen.
         * @param merchantAccount Your merchant account with Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull String merchantAccount) {
            super(shopperLocale, environment);
            mBuilderMerchantAccount = merchantAccount;
        }

        @NonNull
        @Override
        public GooglePayConfiguration build() {
            return new GooglePayConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderMerchantAccount,
                    mBuilderGooglePayEnvironment,
                    mBuilderAmount,
                    mBuilderMerchantInfo,
                    mBuilderAllowedAuthMethods,
                    mBuilderAllowedCardNetworks,
                    mBuilderAllowPrepaidCards,
                    mBuilderEmailRequired,
                    mBuilderExistingPaymentMethodRequired,
                    mBuilderShippingAddressRequired,
                    mBuilderShippingAddressParameters,
                    mBuilderBillingAddressRequired,
                    mBuilderBillingAddressParameters
            );
        }

        /**
         * Set the merchant account to be put in the payment token from Google to Adyen.
         * @param merchantAccount Your merchant account.
         */
        public void setMerchantAccount(@NonNull String merchantAccount) {
            mBuilderMerchantAccount = merchantAccount;
        }

        /**
         * Set the environment to be used by GooglePay.
         * Should be either {@link WalletConstants#ENVIRONMENT_TEST} or {@link WalletConstants#ENVIRONMENT_PRODUCTION}
         *
         * @param googlePayEnvironment The GooglePay environment.
         */
        public void setGooglePayEnvironment(int googlePayEnvironment) {
            if (googlePayEnvironment != WalletConstants.ENVIRONMENT_TEST && googlePayEnvironment != WalletConstants.ENVIRONMENT_PRODUCTION) {
                throw new CheckoutException("Invalid value for Google Environment. "
                        + "Use either WalletConstants.ENVIRONMENT_TEST or WalletConstants.ENVIRONMENT_PRODUCTION");
            }
            mBuilderGooglePayEnvironment = googlePayEnvironment;
        }

        public void setAmount(@NonNull Amount amount) {
            mBuilderAmount = amount;
        }

        public void setBuilderMerchantInfo(@Nullable MerchantInfo builderMerchantInfo) {
            mBuilderMerchantInfo = builderMerchantInfo;
        }

        public void setAllowedAuthMethods(@Nullable List<String> allowedAuthMethods) {
            mBuilderAllowedAuthMethods = allowedAuthMethods;
        }

        public void setAllowedCardNetworks(@Nullable List<String> allowedCardNetworks) {
            mBuilderAllowedCardNetworks = allowedCardNetworks;
        }

        public void setAllowPrepaidCards(boolean allowPrepaidCards) {
            mBuilderAllowPrepaidCards = allowPrepaidCards;
        }

        public void setEmailRequired(boolean builderEmailRequired) {
            mBuilderEmailRequired = builderEmailRequired;
        }

        public void setExistingPaymentMethodRequired(boolean builderExistingPaymentMethodRequired) {
            mBuilderExistingPaymentMethodRequired = builderExistingPaymentMethodRequired;
        }

        public void setShippingAddressRequired(boolean builderShippingAddressRequired) {
            mBuilderShippingAddressRequired = builderShippingAddressRequired;
        }

        public void setShippingAddressParameters(@Nullable ShippingAddressParameters builderShippingAddressParameters) {
            mBuilderShippingAddressParameters = builderShippingAddressParameters;
        }

        public void setBillingAddressRequired(boolean builderBillingAddressRequired) {
            mBuilderBillingAddressRequired = builderBillingAddressRequired;
        }

        public void setBillingAddressParameters(@Nullable BillingAddressParameters builderBillingAddressParameters) {
            mBuilderBillingAddressParameters = builderBillingAddressParameters;
        }
    }
}
