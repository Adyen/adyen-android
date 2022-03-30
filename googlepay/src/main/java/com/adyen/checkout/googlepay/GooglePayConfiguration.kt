/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 4/7/2019.
 */

package com.adyen.checkout.googlepay;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.components.base.AmountConfiguration;
import com.adyen.checkout.components.base.AmountConfigurationBuilder;
import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.components.model.payments.Amount;
import com.adyen.checkout.components.util.CheckoutCurrency;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.ParcelUtils;
import com.adyen.checkout.googlepay.model.BillingAddressParameters;
import com.adyen.checkout.googlepay.model.MerchantInfo;
import com.adyen.checkout.googlepay.model.ShippingAddressParameters;
import com.adyen.checkout.googlepay.util.AllowedAuthMethods;
import com.google.android.gms.wallet.WalletConstants;

import java.util.List;
import java.util.Locale;

public class GooglePayConfiguration extends Configuration implements AmountConfiguration {

    private final String mMerchantAccount;
    private final int mGooglePayEnvironment;
    private final Amount mAmount;
    private final String mTotalPriceStatus;
    private final String mCountryCode;
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

    public static final Parcelable.Creator<GooglePayConfiguration> CREATOR = new Parcelable.Creator<GooglePayConfiguration>() {
        public GooglePayConfiguration createFromParcel(@NonNull Parcel in) {
            return new GooglePayConfiguration(in);
        }

        public GooglePayConfiguration[] newArray(int size) {
            return new GooglePayConfiguration[size];
        }
    };

    GooglePayConfiguration(@NonNull Builder builder) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());
        mMerchantAccount = builder.mBuilderMerchantAccount;
        mGooglePayEnvironment = builder.mBuilderGooglePayEnvironment;
        mAmount = builder.mBuilderAmount;
        mTotalPriceStatus = builder.mBuilderTotalPriceStatus;
        mCountryCode = builder.mBuilderCountryCode;
        mMerchantInfo = builder.mBuilderMerchantInfo;
        mAllowedAuthMethods = builder.mBuilderAllowedAuthMethods;
        mAllowedCardNetworks = builder.mBuilderAllowedCardNetworks;
        mAllowPrepaidCards = builder.mBuilderAllowPrepaidCards;
        mEmailRequired = builder.mBuilderEmailRequired;
        mExistingPaymentMethodRequired = builder.mBuilderExistingPaymentMethodRequired;
        mShippingAddressRequired = builder.mBuilderShippingAddressRequired;
        mShippingAddressParameters = builder.mBuilderShippingAddressParameters;
        mBillingAddressRequired = builder.mBuilderBillingAddressRequired;
        mBillingAddressParameters = builder.mBuilderBillingAddressParameters;
    }

    GooglePayConfiguration(@NonNull Parcel in) {
        super(in);
        mMerchantAccount = in.readString();
        mGooglePayEnvironment = in.readInt();
        mAmount = in.readParcelable(Amount.class.getClassLoader());
        mTotalPriceStatus = in.readString();
        mCountryCode = in.readString();
        mMerchantInfo = in.readParcelable(MerchantInfo.class.getClassLoader());
        mAllowedAuthMethods = in.readArrayList(String.class.getClassLoader());
        mAllowedCardNetworks = in.readArrayList(String.class.getClassLoader());
        mAllowPrepaidCards = ParcelUtils.readBoolean(in);
        mEmailRequired = ParcelUtils.readBoolean(in);
        mExistingPaymentMethodRequired = ParcelUtils.readBoolean(in);
        mShippingAddressRequired = ParcelUtils.readBoolean(in);
        mShippingAddressParameters = in.readParcelable(ShippingAddressParameters.class.getClassLoader());
        mBillingAddressRequired = ParcelUtils.readBoolean(in);
        mBillingAddressParameters = in.readParcelable(BillingAddressParameters.class.getClassLoader());
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mMerchantAccount);
        dest.writeInt(mGooglePayEnvironment);
        dest.writeParcelable(mAmount, flags);
        dest.writeString(mTotalPriceStatus);
        dest.writeString(mCountryCode);
        dest.writeParcelable(mMerchantInfo, flags);
        dest.writeList(mAllowedAuthMethods);
        dest.writeList(mAllowedCardNetworks);
        ParcelUtils.writeBoolean(dest, mAllowPrepaidCards);
        ParcelUtils.writeBoolean(dest, mEmailRequired);
        ParcelUtils.writeBoolean(dest, mExistingPaymentMethodRequired);
        ParcelUtils.writeBoolean(dest, mShippingAddressRequired);
        dest.writeParcelable(mShippingAddressParameters, flags);
        ParcelUtils.writeBoolean(dest, mBillingAddressRequired);
        dest.writeParcelable(mBillingAddressParameters, flags);
    }

    @Nullable
    public String getMerchantAccount() {
        return mMerchantAccount;
    }

    @NonNull
    @Override
    public Amount getAmount() {
        return mAmount;
    }

    @NonNull
    public String getTotalPriceStatus() {
        return mTotalPriceStatus;
    }

    @Nullable
    public String getCountryCode() {
        return mCountryCode;
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
     * Builder to create a {@link GooglePayConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<GooglePayConfiguration> implements AmountConfigurationBuilder {

        private static final String DEFAULT_TOTAL_PRICE_STATUS = "FINAL";

        private String mBuilderMerchantAccount;
        private int mBuilderGooglePayEnvironment = getDefaultGooglePayEnvironment(getBuilderEnvironment());
        private Amount mBuilderAmount = createDefaultAmount();
        private MerchantInfo mBuilderMerchantInfo = null;
        private String mBuilderCountryCode = null;
        private List<String> mBuilderAllowedAuthMethods = AllowedAuthMethods.getAllAllowedAuthMethods();
        private List<String> mBuilderAllowedCardNetworks = null;
        private boolean mBuilderAllowPrepaidCards = false;
        private boolean mBuilderEmailRequired;
        private boolean mBuilderExistingPaymentMethodRequired;
        private boolean mBuilderShippingAddressRequired;
        private ShippingAddressParameters mBuilderShippingAddressParameters;
        private boolean mBuilderBillingAddressRequired;
        private BillingAddressParameters mBuilderBillingAddressParameters;
        private String mBuilderTotalPriceStatus = DEFAULT_TOTAL_PRICE_STATUS;

        private boolean mBuilderIsGoogleEnvironmentSetManually = false;

        private int getDefaultGooglePayEnvironment(Environment environment) {
            if (environment.equals(Environment.TEST)) {
                return WalletConstants.ENVIRONMENT_TEST;
            }
            return WalletConstants.ENVIRONMENT_PRODUCTION;
        }

        private static Amount createDefaultAmount() {
            final Amount defaultAmount = new Amount();
            defaultAmount.setValue(0);
            defaultAmount.setCurrency(CheckoutCurrency.USD.name());
            return defaultAmount;
        }

        /**
         * Builder with required parameters.
         *
         * @param context   A context to get some information.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String clientKey) {
            super(context, clientKey);
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The locale of the Shopper for translation.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         * @param clientKey     Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull String clientKey) {
            super(shopperLocale, environment, clientKey);
        }

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        public Builder(@NonNull GooglePayConfiguration configuration) {
            super(configuration);
            mBuilderMerchantAccount = configuration.getMerchantAccount();
            mBuilderGooglePayEnvironment = configuration.getGooglePayEnvironment();
            mBuilderAmount = configuration.getAmount();
            mBuilderTotalPriceStatus = configuration.getTotalPriceStatus();
            mBuilderCountryCode = configuration.getCountryCode();
            mBuilderMerchantInfo = configuration.getMerchantInfo();
            mBuilderAllowedAuthMethods = configuration.getAllowedAuthMethods();
            mBuilderAllowedCardNetworks = configuration.getAllowedCardNetworks();
            mBuilderAllowPrepaidCards = configuration.isAllowPrepaidCards();
            mBuilderEmailRequired = configuration.isEmailRequired();
            mBuilderExistingPaymentMethodRequired = configuration.isExistingPaymentMethodRequired();
            mBuilderShippingAddressRequired = configuration.isShippingAddressRequired();
            mBuilderShippingAddressParameters = configuration.getShippingAddressParameters();
            mBuilderBillingAddressRequired = configuration.isBillingAddressRequired();
            mBuilderBillingAddressParameters = configuration.getBillingAddressParameters();
        }

        @Override
        @NonNull
        public Builder setShopperLocale(@NonNull Locale builderShopperLocale) {
            return (Builder) super.setShopperLocale(builderShopperLocale);
        }

        @Override
        @NonNull
        public Builder setEnvironment(@NonNull Environment builderEnvironment) {
            if (!mBuilderIsGoogleEnvironmentSetManually) {
                mBuilderGooglePayEnvironment = getDefaultGooglePayEnvironment(builderEnvironment);
            }
            return (Builder) super.setEnvironment(builderEnvironment);
        }

        public void setTotalPriceStatus(@Nullable String builderTotalPriceStatus) {
            mBuilderTotalPriceStatus = builderTotalPriceStatus;
        }

        @NonNull
        @Override
        protected GooglePayConfiguration buildInternal() {
            return new GooglePayConfiguration(this);
        }

        /**
         * Set the merchant account to be put in the payment token from Google to Adyen.
         *
         * @param merchantAccount Your merchant account.
         */
        @NonNull
        public Builder setMerchantAccount(@NonNull String merchantAccount) {
            mBuilderMerchantAccount = merchantAccount;
            return this;
        }

        /**
         * Set the environment to be used by GooglePay.
         * Should be either {@link WalletConstants#ENVIRONMENT_TEST} or {@link WalletConstants#ENVIRONMENT_PRODUCTION}
         *
         * @param googlePayEnvironment The GooglePay environment.
         */
        @NonNull
        public Builder setGooglePayEnvironment(int googlePayEnvironment) {
            if (googlePayEnvironment != WalletConstants.ENVIRONMENT_TEST && googlePayEnvironment != WalletConstants.ENVIRONMENT_PRODUCTION) {
                throw new CheckoutException("Invalid value for Google Environment. "
                        + "Use either WalletConstants.ENVIRONMENT_TEST or WalletConstants.ENVIRONMENT_PRODUCTION");
            }
            mBuilderGooglePayEnvironment = googlePayEnvironment;
            mBuilderIsGoogleEnvironmentSetManually = true;
            return this;
        }

        @NonNull
        @Override
        public Builder setAmount(@NonNull Amount amount) {
            if (!CheckoutCurrency.isSupported(amount.getCurrency()) || amount.getValue() < 0) {
                throw new CheckoutException("Currency is not valid.");
            }
            mBuilderAmount = amount;
            return this;
        }

        @NonNull
        public Builder setBuilderMerchantInfo(@Nullable MerchantInfo builderMerchantInfo) {
            mBuilderMerchantInfo = builderMerchantInfo;
            return this;
        }

        @NonNull
        public Builder setCountryCode(@Nullable String countryCode) {
            mBuilderCountryCode = countryCode;
            return this;
        }

        @NonNull
        public Builder setAllowedAuthMethods(@Nullable List<String> allowedAuthMethods) {
            mBuilderAllowedAuthMethods = allowedAuthMethods;
            return this;
        }

        @NonNull
        public Builder setAllowedCardNetworks(@Nullable List<String> allowedCardNetworks) {
            mBuilderAllowedCardNetworks = allowedCardNetworks;
            return this;
        }

        @NonNull
        public Builder setAllowPrepaidCards(boolean allowPrepaidCards) {
            mBuilderAllowPrepaidCards = allowPrepaidCards;
            return this;
        }

        @NonNull
        public Builder setEmailRequired(boolean builderEmailRequired) {
            mBuilderEmailRequired = builderEmailRequired;
            return this;
        }

        @NonNull
        public Builder setExistingPaymentMethodRequired(boolean builderExistingPaymentMethodRequired) {
            mBuilderExistingPaymentMethodRequired = builderExistingPaymentMethodRequired;
            return this;
        }

        @NonNull
        public Builder setShippingAddressRequired(boolean builderShippingAddressRequired) {
            mBuilderShippingAddressRequired = builderShippingAddressRequired;
            return this;
        }

        @NonNull
        public Builder setShippingAddressParameters(@Nullable ShippingAddressParameters builderShippingAddressParameters) {
            mBuilderShippingAddressParameters = builderShippingAddressParameters;
            return this;
        }

        @NonNull
        public Builder setBillingAddressRequired(boolean builderBillingAddressRequired) {
            mBuilderBillingAddressRequired = builderBillingAddressRequired;
            return this;
        }

        @NonNull
        public Builder setBillingAddressParameters(@Nullable BillingAddressParameters builderBillingAddressParameters) {
            mBuilderBillingAddressParameters = builderBillingAddressParameters;
            return this;
        }
    }
}
