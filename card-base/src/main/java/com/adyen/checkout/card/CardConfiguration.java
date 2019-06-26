/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */

package com.adyen.checkout.card;

import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.adyen.checkout.base.Configuration;
import com.adyen.checkout.base.PaymentComponent;
import com.adyen.checkout.card.model.CardType;
import com.adyen.checkout.core.api.Environment;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link Configuration} class required by {@link PaymentComponent} to change it's behavior.
 * {@link CardComponent#PROVIDER}
 */
public class CardConfiguration implements Configuration {

    private static final CardType[] DEFAULT_SUPPORTED_CARD =
            new CardType[]{CardType.VISA, CardType.AMERICAN_EXPRESS, CardType.MASTERCARD};

    private final String mPublicKey;
    private final List<CardType> mSupportedCardTypes;
    private final boolean mHolderNameRequire;
    private final Locale mShopperLocale;
    private final DisplayMetrics mDisplayMetrics;
    private final Environment mEnvironment;

    @NonNull
    public static CardConfiguration getDefault(
            @NonNull Locale shopperLocale,
            @NonNull DisplayMetrics displayMetrics,
            @NonNull Environment environment,
            @NonNull String publicKey) {
        return new CardConfiguration.Builder(shopperLocale, displayMetrics, environment, publicKey).build();
    }

    /**
     * Constructs a {@link CardConfiguration} object.
     *
     * @param shopperLocale     {@link Locale}
     * @param publicKey         {@link String}
     * @param holderNameRequire {@link Boolean}
     * @param supportCardTypes  {@link CardType}
     */
    public CardConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull DisplayMetrics displayMetrics,
            @NonNull Environment environment,
            @NonNull String publicKey,
            boolean holderNameRequire,
            @NonNull CardType... supportCardTypes) {
        this.mShopperLocale = shopperLocale;
        this.mPublicKey = publicKey;
        this.mEnvironment = environment;
        this.mDisplayMetrics = displayMetrics;
        this.mHolderNameRequire = holderNameRequire;
        this.mSupportedCardTypes = Collections.unmodifiableList(Arrays.asList(supportCardTypes));
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
     * Get supported card types.
     *
     * @return return list of {@link CardType}
     */
    @NonNull
    public List<CardType> getSupportedCardTypes() {
        return mSupportedCardTypes;
    }

    public boolean isHolderNameRequire() {
        return mHolderNameRequire;
    }

    /**
     * Get display metrics.
     *
     * @return {@link DisplayMetrics}
     */
    @NonNull
    public DisplayMetrics getDisplayMetrics() {
        return mDisplayMetrics;
    }

    /**
     * Get display metrics.
     *
     * @return {@link Environment}
     */
    @NonNull
    public Environment getEnvironment() {
        return mEnvironment;
    }

    /**
     * Get shopper's locale.
     *
     * @return {@link Locale}
     */
    @NonNull
    @Override
    public Locale getShopperLocale() {
        return mShopperLocale;
    }

    /**
     * Card Configuration Builder.
     */
    public static final class Builder {

        private final String mPublicKey;
        private final Locale mShopperLocale;
        private final DisplayMetrics mDisplayMetrics;
        private final Environment mEnvironment;

        private CardType[] mSupportedCardTypes = DEFAULT_SUPPORTED_CARD;
        private boolean mHolderNameRequire;

        /**
         * Constructor of Card Configuration Builder.
         *
         * @param shopperLocale {@link Locale}
         * @param publicKey     {@link String}
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull DisplayMetrics displayMetrics,
                @NonNull Environment environment,
                @NonNull String publicKey) {
            this.mShopperLocale = shopperLocale;
            this.mDisplayMetrics = displayMetrics;
            this.mEnvironment = environment;
            this.mPublicKey = publicKey;
        }

        /**
         * Set supported card types for card-payment.
         *
         * @param supportCardTypes array of {@link CardType}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSupportedCardTypes(@NonNull CardType... supportCardTypes) {
            this.mSupportedCardTypes = supportCardTypes;
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
            this.mHolderNameRequire = holderNameRequire;
            return this;
        }

        /**
         * Build {@link CardConfiguration} object from {@link CardConfiguration.Builder} inputs.
         *
         * @return {@link CardConfiguration}
         */
        @NonNull
        public CardConfiguration build() {
            return new CardConfiguration(mShopperLocale, mDisplayMetrics, mEnvironment, mPublicKey, mHolderNameRequire, mSupportedCardTypes);
        }
    }

}
