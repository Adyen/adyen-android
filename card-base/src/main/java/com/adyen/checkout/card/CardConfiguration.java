/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.component.BaseConfiguration;
import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.util.ParcelUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link Configuration} class required by {@link CardComponent} to change it's behavior. Pass it to the {@link CardComponent#PROVIDER}.
 */
public class CardConfiguration extends BaseConfiguration {

    private static final CardType[] DEFAULT_SUPPORTED_CARDS =
            new CardType[]{CardType.VISA, CardType.AMERICAN_EXPRESS, CardType.MASTERCARD};

    public static final List<CardType> DEFAULT_SUPPORTED_CARDS_LIST =
            Collections.unmodifiableList(Arrays.asList(DEFAULT_SUPPORTED_CARDS));

    private final String mPublicKey;
    private final String mShopperReference;
    private final boolean mHolderNameRequire;
    private final List<CardType> mSupportedCardTypes;
    private final boolean mShowStorePaymentField;

    public static final Parcelable.Creator<CardConfiguration> CREATOR = new Parcelable.Creator<CardConfiguration>() {
        public CardConfiguration createFromParcel(@NonNull Parcel in) {
            return new CardConfiguration(in);
        }

        public CardConfiguration[] newArray(int size) {
            return new CardConfiguration[size];
        }
    };

    /**
     * @param shopperLocale         The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment           The environment to be used to make network calls.
     * @param displayMetrics        The current {@link DisplayMetrics} of the device to fetch images of matching size.
     * @param publicKey             The public key used for encryption of the card data. You can get it from the Customer Area.
     * @param shopperReference      The unique identifier of the shopper.
     * @param holderNameRequire     If the holder name of the card should be shown as a required field.
     * @param showStorePaymentField If the component should show the option to store the card for later use.
     * @param supportCardTypes      The list of supported card brands to be shown to the user.
     * @deprecated Constructor with all parameters. Use the Builder to initialize this object.
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public CardConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @SuppressWarnings("PMD.UnusedFormalParameter")
            @NonNull DisplayMetrics displayMetrics,
            @NonNull String publicKey,
            boolean holderNameRequire,
            @NonNull String shopperReference,
            boolean showStorePaymentField,
            @NonNull CardType... supportCardTypes) {
        super(shopperLocale, environment);

        mPublicKey = publicKey;
        mHolderNameRequire = holderNameRequire;
        mSupportedCardTypes = Collections.unmodifiableList(Arrays.asList(supportCardTypes));
        mShopperReference = shopperReference;
        mShowStorePaymentField = showStorePaymentField;
    }


    /**
     * @param shopperLocale         The locale that should be used to display strings and layouts. Can differ from device default.
     * @param environment           The environment to be used to make network calls.
     * @param publicKey             The public key used for encryption of the card data. You can get it from the Customer Area.
     * @param shopperReference      The unique identifier of the shopper.
     * @param holderNameRequire     If the holder name of the card should be shown as a required field.
     * @param showStorePaymentField If the component should show the option to store the card for later use.
     * @param supportCardTypes      The list of supported card brands to be shown to the user.
     */
    CardConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @NonNull String publicKey,
            boolean holderNameRequire,
            @NonNull String shopperReference,
            boolean showStorePaymentField,
            @NonNull List<CardType> supportCardTypes) {
        super(shopperLocale, environment);

        mPublicKey = publicKey;
        mHolderNameRequire = holderNameRequire;
        mSupportedCardTypes = supportCardTypes;
        mShopperReference = shopperReference;
        mShowStorePaymentField = showStorePaymentField;
    }

    CardConfiguration(@NonNull Parcel in) {
        super(in);
        mPublicKey = in.readString();
        mShopperReference = in.readString();
        mHolderNameRequire = ParcelUtils.readBoolean(in);
        mSupportedCardTypes = in.readArrayList(CardType.class.getClassLoader());
        mShowStorePaymentField = ParcelUtils.readBoolean(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mPublicKey);
        dest.writeString(mShopperReference);
        ParcelUtils.writeBoolean(dest, mHolderNameRequire);
        dest.writeList(mSupportedCardTypes);
        ParcelUtils.writeBoolean(dest, mShowStorePaymentField);
    }

    /**
     * Get display metrics.
     *
     * @return {@link DisplayMetrics}
     * @deprecated There is no need for {@link DisplayMetrics} in builder any more, it'll always return null
     */
    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    @Nullable
    public DisplayMetrics getDisplayMetrics() {
        return null;
    }

    /**
     * Get public key.
     *
     * @return {@link String}
     */
    @NonNull
    public String getPublicKey() {
        return mPublicKey;
    }

    /**
     * The list of {@link CardType} that this payment supports. Used to predict the card type of the
     *
     * @return The list of {@link CardType}.
     */
    @NonNull
    public List<CardType> getSupportedCardTypes() {
        return mSupportedCardTypes;
    }

    /**
     * @return If the Holder Name is required for this Card payment.
     */
    public boolean isHolderNameRequire() {
        return mHolderNameRequire;
    }

    @Nullable
    public String getShopperReference() {
        return mShopperReference;
    }

    public boolean isShowStorePaymentFieldEnable() {
        return mShowStorePaymentField;
    }

    @NonNull
    public Builder newBuilder() {
        return new Builder(this);
    }

    /**
     * Builder to create a {@link CardConfiguration} more easily.
     */
    public static final class Builder extends BaseConfigurationBuilder<CardConfiguration> {

        private String mBuilderPublicKey;

        private List<CardType> mBuilderSupportedCardTypes = DEFAULT_SUPPORTED_CARDS_LIST;
        private boolean mBuilderHolderNameRequire;
        private boolean mBuilderShowStorePaymentField = true;
        private String mShopperReference;


        /**
         * Constructor of Card Configuration Builder with instance of CardConfiguration.
         */
        public Builder(@NonNull CardConfiguration cardConfiguration) {
            super(cardConfiguration.getShopperLocale(), cardConfiguration.getEnvironment());

            mBuilderPublicKey = cardConfiguration.getPublicKey();
            mBuilderSupportedCardTypes = cardConfiguration.getSupportedCardTypes();
            mBuilderHolderNameRequire = cardConfiguration.isHolderNameRequire();
            mBuilderShowStorePaymentField = cardConfiguration.isShowStorePaymentFieldEnable();
            mShopperReference = cardConfiguration.getShopperReference();
        }

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public Builder(@NonNull Context context, @NonNull String publicKey) {
            super(context);
            mBuilderPublicKey = publicKey;
        }

        /**
         * Builder with required parameters for a {@link CardConfiguration}.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         * @param publicKey     The public key used for encryption of the card data. You can get it from the Customer Area.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderPublicKey = publicKey;
        }

        /**
         * Builder with required parameters for a {@link CardConfiguration}.
         *
         * @param shopperLocale  The Locale of the shopper.
         * @param environment    The {@link Environment} to be used for network calls to Adyen.
         * @param displayMetrics The DisplayMetrics to fetch images with the correct size.
         * @param publicKey      The public key used for encryption of the card data. You can get it from the Customer Area.
         * @deprecated No need to pass {@link DisplayMetrics} to builder.
         */
        @Deprecated
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @SuppressWarnings("PMD.UnusedFormalParameter")
                @Nullable DisplayMetrics displayMetrics,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderPublicKey = publicKey;
        }

        /**
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        public void setPublicKey(@NonNull String publicKey) {
            mBuilderPublicKey = publicKey;
        }

        /**
         * Set supported card types for card-payment.
         *
         * @param supportCardTypes array of {@link CardType}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSupportedCardTypes(@NonNull CardType... supportCardTypes) {
            this.mBuilderSupportedCardTypes = Arrays.asList(supportCardTypes);
            return this;
        }

        /**
         * Set that if holder name require.
         *
         * @param holderNameRequire {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHolderNameRequire(boolean holderNameRequire) {
            this.mBuilderHolderNameRequire = holderNameRequire;
            return this;
        }

        /**
         * Show store payment field.
         *
         * @param showStorePaymentField {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setShowStorePaymentField(boolean showStorePaymentField) {
            this.mBuilderShowStorePaymentField = showStorePaymentField;
            return this;
        }

        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            this.mShopperReference = shopperReference;
            return this;
        }

        /**
         * Build {@link CardConfiguration} object from {@link CardConfiguration.Builder} inputs.
         *
         * @return {@link CardConfiguration}
         */
        @NonNull
        public CardConfiguration build() {
            return new CardConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderPublicKey,
                    mBuilderHolderNameRequire,
                    mShopperReference,
                    mBuilderShowStorePaymentField,
                    mBuilderSupportedCardTypes
            );
        }
    }

}
