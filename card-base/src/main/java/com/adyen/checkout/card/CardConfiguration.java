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

import com.adyen.checkout.base.component.BaseConfigurationBuilder;
import com.adyen.checkout.base.component.Configuration;
import com.adyen.checkout.base.util.ValidationUtils;
import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.exception.CheckoutException;
import com.adyen.checkout.core.util.ParcelUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * {@link Configuration} class required by {@link CardComponent} to change it's behavior. Pass it to the {@link CardComponent#PROVIDER}.
 */
public class CardConfiguration extends Configuration {

    private static final CardType[] DEFAULT_SUPPORTED_CARDS =
            new CardType[]{CardType.VISA, CardType.AMERICAN_EXPRESS, CardType.MASTERCARD};

    // BCMC is only supported in it's own component.
    private static final CardType[] UNSUPPORTED_CARDS = new CardType[]{CardType.BCMC};

    public static final List<CardType> DEFAULT_SUPPORTED_CARDS_LIST =
            Collections.unmodifiableList(Arrays.asList(DEFAULT_SUPPORTED_CARDS));

    private final String mPublicKey;
    private final String mShopperReference;
    private final boolean mHolderNameRequire;
    private final List<CardType> mSupportedCardTypes;
    private final boolean mShowStorePaymentField;
    private final boolean mHideCvc;
    private final boolean mHideCvcStoredCard;

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
     * @param publicKey             The public key used for encryption of the card data. You can get it from the Customer Area.
     * @param shopperReference      The unique identifier of the shopper.
     * @param holderNameRequire     If the holder name of the card should be shown as a required field.
     * @param showStorePaymentField If the component should show the option to store the card for later use.
     * @param supportCardTypes      The list of supported card brands to be shown to the user.
     * @param hideCvc               Hides the CVC field on the payment flow so that it's not required.
     * @param hideCvcStoredCard     Hides the CVC field on the stored payment flow so that it's not required.
     */
    CardConfiguration(
            @NonNull Locale shopperLocale,
            @NonNull Environment environment,
            @Nullable String clientKey,
            @NonNull String publicKey,
            boolean holderNameRequire,
            @NonNull String shopperReference,
            boolean showStorePaymentField,
            @NonNull List<CardType> supportCardTypes,
            boolean hideCvc,
            boolean hideCvcStoredCard
    ) {
        super(shopperLocale, environment, clientKey);

        mPublicKey = publicKey;
        mHolderNameRequire = holderNameRequire;
        mSupportedCardTypes = supportCardTypes;
        mShopperReference = shopperReference;
        mShowStorePaymentField = showStorePaymentField;
        mHideCvc = hideCvc;
        mHideCvcStoredCard = hideCvcStoredCard;
    }

    CardConfiguration(@NonNull Parcel in) {
        super(in);
        mPublicKey = in.readString();
        mShopperReference = in.readString();
        mHolderNameRequire = ParcelUtils.readBoolean(in);
        mSupportedCardTypes = in.readArrayList(CardType.class.getClassLoader());
        mShowStorePaymentField = ParcelUtils.readBoolean(in);
        mHideCvc = ParcelUtils.readBoolean(in);
        mHideCvcStoredCard = ParcelUtils.readBoolean(in);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mPublicKey);
        dest.writeString(mShopperReference);
        ParcelUtils.writeBoolean(dest, mHolderNameRequire);
        dest.writeList(mSupportedCardTypes);
        ParcelUtils.writeBoolean(dest, mShowStorePaymentField);
        ParcelUtils.writeBoolean(dest, mHideCvc);
        ParcelUtils.writeBoolean(dest, mHideCvcStoredCard);
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

    @Nullable
    public boolean isHideCvc() {
        return mHideCvc;
    }

    @Nullable
    public boolean isHideCvcStoredCard() {
        return mHideCvcStoredCard;
    }

    /**
     * Builder to create a {@link CardConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<CardConfiguration> {

        private String mBuilderPublicKey;

        private List<CardType> mBuilderSupportedCardTypes = Collections.emptyList();
        private boolean mBuilderHolderNameRequire;
        private boolean mBuilderShowStorePaymentField = true;
        private String mShopperReference;
        private boolean mBuilderHideCvc;
        private boolean mBuilderHideCvcStoredCard;

        /**
         * Constructor of Card Configuration Builder with instance of CardConfiguration.
         */
        public Builder(@NonNull CardConfiguration cardConfiguration) {
            super(cardConfiguration.getShopperLocale(), cardConfiguration.getEnvironment());
            mBuilderClientKey = cardConfiguration.getClientKey();

            mBuilderPublicKey = cardConfiguration.getPublicKey();
            mBuilderSupportedCardTypes = cardConfiguration.getSupportedCardTypes();
            mBuilderHolderNameRequire = cardConfiguration.isHolderNameRequire();
            mBuilderShowStorePaymentField = cardConfiguration.isShowStorePaymentFieldEnable();
            mShopperReference = cardConfiguration.getShopperReference();
            mBuilderHideCvc = cardConfiguration.isHideCvc();
            mBuilderHideCvcStoredCard = cardConfiguration.isHideCvcStoredCard();
        }

        /**
         * Constructor of Card Configuration Builder with default values from Context.
         *
         * @param context   A context
         */
        public Builder(@NonNull Context context) {
            super(context);
        }

        /**
         * Builder with parameters for a {@link CardConfiguration}.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The {@link Environment} to be used for network calls to Adyen.
         */
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment) {
            super(shopperLocale, environment);
        }

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         * @deprecated      Constructor deprecated since publicKey is no longer always required in favor of clientKey.
         */
        @Deprecated
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
         * @deprecated          Constructor deprecated since publicKey is no longer always required in favor of clientKey.
         */
        @Deprecated
        public Builder(
                @NonNull Locale shopperLocale,
                @NonNull Environment environment,
                @NonNull String publicKey) {
            super(shopperLocale, environment);
            mBuilderPublicKey = publicKey;
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

        /**
         * @param publicKey The public key to be used for encryption. You can get it from the Customer Area.
         */
        @NonNull
        public Builder setPublicKey(@NonNull String publicKey) {
            mBuilderPublicKey = publicKey;
            return this;
        }

        /**
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * @param supportCardTypes array of {@link CardType}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSupportedCardTypes(@NonNull CardType... supportCardTypes) {

            final List<CardType> supportedCards = new ArrayList<>(Arrays.asList(supportCardTypes));
            supportedCards.removeAll(Arrays.asList(UNSUPPORTED_CARDS));

            mBuilderSupportedCardTypes = supportedCards;
            return this;
        }

        /**
         * Set if the holder name is required and should be shown as an input field.
         *
         * @param holderNameRequire {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHolderNameRequire(boolean holderNameRequire) {
            mBuilderHolderNameRequire = holderNameRequire;
            return this;
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * @param showStorePaymentField {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setShowStorePaymentField(boolean showStorePaymentField) {
            mBuilderShowStorePaymentField = showStorePaymentField;
            return this;
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the {@link com.adyen.checkout.base.model.payments.request.PaymentComponentData}
         * for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setShopperReference(@NonNull String shopperReference) {
            mShopperReference = shopperReference;
            return this;
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a regular payment.
         * Note that this might have implications for the risk of the transaction. Talk to Adyen Support before enabling this.
         *
         * @param hideCvc If CVC should be hidden or not.
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHideCvc(boolean hideCvc) {
            mBuilderHideCvc = hideCvc;
            return this;
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a stored payment flow.
         * Note that this has implications for the risk of the transaction. Talk to Adyen Support before enabling this.
         *
         * @param hideCvcStoredCard If CVC should be hidden or not for stored payments.
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHideCvcStoredCard(boolean hideCvcStoredCard) {
            mBuilderHideCvcStoredCard = hideCvcStoredCard;
            return this;
        }

        /**
         * Build {@link CardConfiguration} object from {@link CardConfiguration.Builder} inputs.
         *
         * @return {@link CardConfiguration}
         */
        @NonNull
        public CardConfiguration build() {

            if (!CardValidationUtils.isPublicKeyValid(mBuilderPublicKey)) {
                throw new CheckoutException("Invalid Public Key. Please find the valid public key on the Customer Area.");
            }

            // This will not be triggered until the public key check above is removed as it takes priority.
            if (!CardValidationUtils.isPublicKeyValid(mBuilderPublicKey) && !ValidationUtils.isClientKeyValid(mBuilderClientKey)) {
                throw new CheckoutException("You need either a valid Client key or Public key to use the Card Component.");
            }

            return new CardConfiguration(
                    mBuilderShopperLocale,
                    mBuilderEnvironment,
                    mBuilderClientKey,
                    mBuilderPublicKey,
                    mBuilderHolderNameRequire,
                    mShopperReference,
                    mBuilderShowStorePaymentField,
                    mBuilderSupportedCardTypes,
                    mBuilderHideCvc,
                    mBuilderHideCvcStoredCard
            );
        }
    }

}
