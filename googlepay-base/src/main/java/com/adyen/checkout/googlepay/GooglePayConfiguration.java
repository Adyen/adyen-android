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

import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.model.payments.Amount;
import com.adyen.checkout.base.util.CheckoutCurrency;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.ParcelUtils;
import com.adyen.checkout.googlepay.model.BillingAddressParameters;
import com.adyen.checkout.googlepay.model.MerchantInfo;
import com.adyen.checkout.googlepay.model.ShippingAddressParameters;
import com.adyen.checkout.googlepay.util.AllowedAuthMethods;
import com.adyen.checkout.googlepay.util.AllowedCardNetworks;
import com.google.android.gms.wallet.WalletConstants;

import java.util.List;
import java.util.Locale;

public class GooglePayConfiguration extends Configuration {

    private final String mMerchantAccount;
    private final int mGooglePayEnvironment;
    private final Amount mAmount;
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

    @SuppressWarnings("ParameterNumber")
    GooglePayConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @Nullable String clientKey,
            @NonNull String merchantAccount,
            int googlePayEnvironment,
            @NonNull Amount amount,
            @Nullable String countryCode,
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
        super(shopperLocale, environment, clientKey);
        mMerchantAccount = merchantAccount;
        mGooglePayEnvironment = googlePayEnvironment;
        mAmount = amount;
        mCountryCode = countryCode;
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

    GooglePayConfiguration(@NonNull Parcel in) {
        super(in);
        mMerchantAccount = in.readString();
        mGooglePayEnvironment = in.readInt();
        mAmount = in.readParcelable(Amount.class.getClassLoader());
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

    @NonNull
    public String getMerchantAccount() {
        return mMerchantAccount;
    }

    @NonNull
    public Amount getAmount() {
        return mAmount;
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
    public static final class Builder extends BaseConfigurationBuilder<GooglePayConfiguration> {

        private String mBuilderMerchantAccount;
        private int mBuilderGooglePayEnvironment = WalletConstants.ENVIRONMENT_TEST;
        private Amount mBuilderAmount = createDefaultAmount();
        private MerchantInfo mBuilderMerchantInfo = null;
        private String mBuilderCountryCode = null;
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
         * @param context         A context to get some information.
         * @param merchantAccount Your merchant account with Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String merchantAccount) {
            super(context);
            mBuilderMerchantAccount = merchantAccount;
        }

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale   The locale of the Shopper for translation.
         * @param environment     TThe {@link Environment} to be used for network calls to Adyen.
         * @param merchantAccount Your merchant account with Adyen.
         */
        public Builder(@NonNull Locale shopperLocale, @NonNull Environment environment, @NonNull String merchantAccount) {
            super(shopperLocale, environment);
            mBuilderMerchantAccount = merchantAccount;
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
        public GooglePayConfiguration build() {
            return new GooglePayConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderClientKey,
                    mBuilderMerchantAccount,
                    mBuilderGooglePayEnvironment,
                    mBuilderAmount,
                    mBuilderCountryCode,
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
            return this;
        }

        @NonNull
        public Builder setAmount(@NonNull Amount amount) {
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
