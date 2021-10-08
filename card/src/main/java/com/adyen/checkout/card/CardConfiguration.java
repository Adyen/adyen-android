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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.adyen.checkout.card.data.CardType;
import com.adyen.checkout.components.base.AddressVisibility;
import com.adyen.checkout.components.base.BaseConfigurationBuilder;
import com.adyen.checkout.components.base.Configuration;
import com.adyen.checkout.core.api.Environment;
import com.adyen.checkout.core.util.ParcelUtils;

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

    public static final List<CardType> DEFAULT_SUPPORTED_CARDS_LIST =
            Collections.unmodifiableList(Arrays.asList(DEFAULT_SUPPORTED_CARDS));

    private final String mShopperReference;
    private final boolean mHolderNameRequired;
    private final List<CardType> mSupportedCardTypes;
    private final boolean mShowStorePaymentField;
    private final boolean mHideCvc;
    private final boolean mHideCvcStoredCard;
    private final SocialSecurityNumberVisibility mSocialSecurityNumberVisibility;
    private final KCPAuthVisibility mKcpAuthVisibility;
    private final AddressVisibility mAddressVisibility;

    public static final Parcelable.Creator<CardConfiguration> CREATOR = new Parcelable.Creator<CardConfiguration>() {
        public CardConfiguration createFromParcel(@NonNull Parcel in) {
            return new CardConfiguration(in);
        }

        public CardConfiguration[] newArray(int size) {
            return new CardConfiguration[size];
        }
    };

    /**
     * @param builder The Builder instance to create the configuration.
     */
    CardConfiguration(
            Builder builder
    ) {
        super(builder.getBuilderShopperLocale(), builder.getBuilderEnvironment(), builder.getBuilderClientKey());

        mHolderNameRequired = builder.mBuilderHolderNameRequired;
        mSupportedCardTypes = builder.mBuilderSupportedCardTypes;
        mShopperReference = builder.mShopperReference;
        mShowStorePaymentField = builder.mBuilderShowStorePaymentField;
        mHideCvc = builder.mBuilderHideCvc;
        mHideCvcStoredCard = builder.mBuilderHideCvcStoredCard;
        mSocialSecurityNumberVisibility = builder.mBuilderSocialSecurityNumberVisibility;
        mKcpAuthVisibility = builder.mBuilderKcpAuthVisibility;
        mAddressVisibility = builder.mBuilderAddressVisibility;
    }

    CardConfiguration(@NonNull Parcel in) {
        super(in);
        mShopperReference = in.readString();
        mHolderNameRequired = ParcelUtils.readBoolean(in);
        mSupportedCardTypes = in.readArrayList(CardType.class.getClassLoader());
        mShowStorePaymentField = ParcelUtils.readBoolean(in);
        mHideCvc = ParcelUtils.readBoolean(in);
        mHideCvcStoredCard = ParcelUtils.readBoolean(in);
        mSocialSecurityNumberVisibility = SocialSecurityNumberVisibility.valueOf(in.readString());
        mKcpAuthVisibility = KCPAuthVisibility.valueOf(in.readString());
        mAddressVisibility = (AddressVisibility) in.readSerializable();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(mShopperReference);
        ParcelUtils.writeBoolean(dest, mHolderNameRequired);
        dest.writeList(mSupportedCardTypes);
        ParcelUtils.writeBoolean(dest, mShowStorePaymentField);
        ParcelUtils.writeBoolean(dest, mHideCvc);
        ParcelUtils.writeBoolean(dest, mHideCvcStoredCard);
        dest.writeString(mSocialSecurityNumberVisibility.name());
        dest.writeString(mKcpAuthVisibility.name());
        dest.writeSerializable(mAddressVisibility);
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
    public boolean isHolderNameRequired() {
        return mHolderNameRequired;
    }

    @Nullable
    public String getShopperReference() {
        return mShopperReference;
    }

    /**
     * @deprecated in favor of isStorePaymentFieldVisible because it had a typo.
     */
    @Deprecated
    public boolean isShowStorePaymentFieldEnable() {
        return mShowStorePaymentField;
    }

    public boolean isStorePaymentFieldVisible() {
        return mShowStorePaymentField;
    }

    @NonNull
    public Builder newBuilder() {
        return new Builder(this);
    }

    public boolean isHideCvc() {
        return mHideCvc;
    }

    public boolean isHideCvcStoredCard() {
        return mHideCvcStoredCard;
    }

    @Nullable
    public SocialSecurityNumberVisibility getSocialSecurityNumberVisibility() {
        return mSocialSecurityNumberVisibility;
    }

    @Nullable
    public KCPAuthVisibility getKcpAuthVisibility() {
        return mKcpAuthVisibility;
    }

    @NonNull
    public AddressVisibility getAddressVisibility() {
        return mAddressVisibility;
    }

    /**
     * Builder to create a {@link CardConfiguration}.
     */
    public static final class Builder extends BaseConfigurationBuilder<CardConfiguration> {

        private List<CardType> mBuilderSupportedCardTypes = Collections.emptyList();
        private boolean mBuilderHolderNameRequired;
        private boolean mBuilderShowStorePaymentField = true;
        private String mShopperReference;
        private boolean mBuilderHideCvc;
        private boolean mBuilderHideCvcStoredCard;
        private SocialSecurityNumberVisibility mBuilderSocialSecurityNumberVisibility = SocialSecurityNumberVisibility.HIDE;
        private KCPAuthVisibility mBuilderKcpAuthVisibility = KCPAuthVisibility.HIDE;
        private AddressVisibility mBuilderAddressVisibility = AddressVisibility.NONE;

        /**
         * Constructor of Card Configuration Builder with instance of CardConfiguration.
         */
        public Builder(@NonNull CardConfiguration cardConfiguration) {
            super(cardConfiguration.getShopperLocale(), cardConfiguration.getEnvironment(), cardConfiguration.getClientKey());
            mBuilderSupportedCardTypes = cardConfiguration.getSupportedCardTypes();
            mBuilderHolderNameRequired = cardConfiguration.isHolderNameRequired();
            mBuilderShowStorePaymentField = cardConfiguration.isStorePaymentFieldVisible();
            mShopperReference = cardConfiguration.getShopperReference();
            mBuilderHideCvc = cardConfiguration.isHideCvc();
            mBuilderHideCvcStoredCard = cardConfiguration.isHideCvcStoredCard();
            mBuilderSocialSecurityNumberVisibility = cardConfiguration.getSocialSecurityNumberVisibility();
            mBuilderKcpAuthVisibility = cardConfiguration.getKcpAuthVisibility();
            mBuilderAddressVisibility = cardConfiguration.getAddressVisibility();
        }

        /**
         * Constructor of Card Configuration Builder with default values from Context.
         *
         * @param context A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        public Builder(@NonNull Context context, @NonNull String clientKey) {
            super(context, clientKey);
        }

        /**
         * Builder with parameters for a {@link CardConfiguration}.
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
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * @param supportCardTypes array of {@link CardType}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSupportedCardTypes(@NonNull CardType... supportCardTypes) {
            mBuilderSupportedCardTypes = Arrays.asList(supportCardTypes);
            return this;
        }

        /**
         * Set if the holder name is required and should be shown as an input field.
         *
         * @param holderNameRequired {@link Boolean}
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setHolderNameRequired(boolean holderNameRequired) {
            mBuilderHolderNameRequired = holderNameRequired;
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
         * This value will simply be passed back to you in the {@link com.adyen.checkout.components.model.payments.request.PaymentComponentData}
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
         * Set if CPF/CNPJ field for Brazil merchants should be visible or not.
         *
         * @param socialSecurityNumberVisibility If CPF/CNPJ field should be visible or not.
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setSocialSecurityNumberVisibility(@NonNull SocialSecurityNumberVisibility socialSecurityNumberVisibility) {
            mBuilderSocialSecurityNumberVisibility = socialSecurityNumberVisibility;
            return this;
        }

        @NonNull
        public Builder setKcpAuthVisibility(@NonNull KCPAuthVisibility kcpAuthVisibility) {
            mBuilderKcpAuthVisibility = kcpAuthVisibility;
            return this;
        }

        /**
         * Specifies whether address input fields should be shown or not and in which form.
         *
         * @param addressVisibility The visibility state of the address input fields.
         * @return {@link CardConfiguration.Builder}
         */
        @NonNull
        public Builder setAddressVisibility(@NonNull AddressVisibility addressVisibility) {
            mBuilderAddressVisibility = addressVisibility;
            return this;
        }

        /**
         * Build {@link CardConfiguration} object from {@link CardConfiguration.Builder} inputs.
         *
         * @return {@link CardConfiguration}
         */
        @NonNull
        protected CardConfiguration buildInternal() {
            return new CardConfiguration(this);
        }
    }

}
