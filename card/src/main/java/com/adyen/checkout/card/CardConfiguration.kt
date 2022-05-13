/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */
package com.adyen.checkout.card

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.base.AddressVisibility
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.util.ParcelUtils.readBoolean
import com.adyen.checkout.core.util.ParcelUtils.writeBoolean
import java.util.Locale

/**
 * [Configuration] class required by [CardComponent] to change it's behavior. Pass it to the [CardComponent.PROVIDER].
 */
class CardConfiguration : Configuration {

    val isHolderNameRequired: Boolean
    val supportedCardTypes: List<CardType>
    val shopperReference: String?
    val isStorePaymentFieldVisible: Boolean
    val isHideCvc: Boolean
    val isHideCvcStoredCard: Boolean
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility?
    val kcpAuthVisibility: KCPAuthVisibility?
    val addressVisibility: AddressVisibility
    val installmentConfiguration: InstallmentConfiguration?
    val addressConfiguration: AddressConfiguration?

    @Suppress("LongParameterList")
    internal constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        isHolderNameRequired: Boolean,
        supportedCardTypes: List<CardType>,
        shopperReference: String?,
        isStorePaymentFieldVisible: Boolean,
        isHideCvc: Boolean,
        isHideCvcStoredCard: Boolean,
        socialSecurityNumberVisibility: SocialSecurityNumberVisibility?,
        kcpAuthVisibility: KCPAuthVisibility?,
        addressVisibility: AddressVisibility,
        installmentConfiguration: InstallmentConfiguration?,
        addressConfiguration: AddressConfiguration?
    ) : super(shopperLocale, environment, clientKey) {
        this.isHolderNameRequired = isHolderNameRequired
        this.supportedCardTypes = supportedCardTypes
        this.shopperReference = shopperReference
        this.isStorePaymentFieldVisible = isStorePaymentFieldVisible
        this.isHideCvc = isHideCvc
        this.isHideCvcStoredCard = isHideCvcStoredCard
        this.socialSecurityNumberVisibility = socialSecurityNumberVisibility
        this.kcpAuthVisibility = kcpAuthVisibility
        this.addressVisibility = addressVisibility
        this.installmentConfiguration = installmentConfiguration
        this.addressConfiguration = addressConfiguration
    }

    internal constructor(parcel: Parcel) : super(parcel) {
        shopperReference = parcel.readString()
        isHolderNameRequired = readBoolean(parcel)
        supportedCardTypes = parcel.readArrayList(CardType::class.java.classLoader) as List<CardType>
        isStorePaymentFieldVisible = readBoolean(parcel)
        isHideCvc = readBoolean(parcel)
        isHideCvcStoredCard = readBoolean(parcel)
        socialSecurityNumberVisibility = SocialSecurityNumberVisibility.valueOf(parcel.readString()!!)
        kcpAuthVisibility = KCPAuthVisibility.valueOf(parcel.readString()!!)
        addressVisibility = (parcel.readSerializable() as AddressVisibility?)!!
        installmentConfiguration = parcel.readParcelable(InstallmentConfiguration::class.java.classLoader)
        addressConfiguration = parcel.readParcelable(AddressConfiguration::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(shopperReference)
        writeBoolean(parcel, isHolderNameRequired)
        parcel.writeList(supportedCardTypes)
        writeBoolean(parcel, isStorePaymentFieldVisible)
        writeBoolean(parcel, isHideCvc)
        writeBoolean(parcel, isHideCvcStoredCard)
        parcel.writeString(socialSecurityNumberVisibility!!.name)
        parcel.writeString(kcpAuthVisibility!!.name)
        parcel.writeSerializable(addressVisibility)
        parcel.writeParcelable(installmentConfiguration, flags)
        parcel.writeParcelable(addressConfiguration, flags)
    }

    fun newBuilder(): Builder {
        return Builder(this)
    }

    /**
     * Builder to create a [CardConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder : BaseConfigurationBuilder<CardConfiguration> {
        private var builderSupportedCardTypes: List<CardType> = emptyList()
        private var builderHolderNameRequired = false
        private var builderIsStorePaymentFieldVisible = true
        private var builderShopperReference: String? = null
        private var builderHideCvc = false
        private var builderHideCvcStoredCard = false
        private var builderSocialSecurityNumberVisibility: SocialSecurityNumberVisibility? =
            SocialSecurityNumberVisibility.HIDE
        private var builderKcpAuthVisibility: KCPAuthVisibility? = KCPAuthVisibility.HIDE
        private var builderAddressVisibility = AddressVisibility.NONE
        private var builderInstallmentConfiguration: InstallmentConfiguration? = null
        private var builderAddressConfiguration: AddressConfiguration? = null

        /**
         * Constructor of Card Configuration Builder with instance of CardConfiguration.
         */
        constructor(cardConfiguration: CardConfiguration) : super(cardConfiguration) {
            builderSupportedCardTypes = cardConfiguration.supportedCardTypes
            builderHolderNameRequired = cardConfiguration.isHolderNameRequired
            builderIsStorePaymentFieldVisible = cardConfiguration.isStorePaymentFieldVisible
            builderShopperReference = cardConfiguration.shopperReference
            builderHideCvc = cardConfiguration.isHideCvc
            builderHideCvcStoredCard = cardConfiguration.isHideCvcStoredCard
            builderSocialSecurityNumberVisibility = cardConfiguration.socialSecurityNumberVisibility
            builderKcpAuthVisibility = cardConfiguration.kcpAuthVisibility
            builderAddressVisibility = cardConfiguration.addressVisibility
            builderInstallmentConfiguration = cardConfiguration.installmentConfiguration
            builderAddressConfiguration = cardConfiguration.addressConfiguration
        }

        /**
         * Constructor of Card Configuration Builder with default values from Context.
         *
         * @param context A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, clientKey: String) : super(context, clientKey)

        /**
         * Builder with parameters for a [CardConfiguration].
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        /**
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * @param supportCardTypes array of [CardType]
         * @return [CardConfiguration.Builder]
         */
        fun setSupportedCardTypes(vararg supportCardTypes: CardType): Builder {
            builderSupportedCardTypes = listOf(*supportCardTypes)
            return this
        }

        /**
         * Set if the holder name is required and should be shown as an input field.
         *
         * @param holderNameRequired [Boolean]
         * @return [CardConfiguration.Builder]
         */
        fun setHolderNameRequired(holderNameRequired: Boolean): Builder {
            builderHolderNameRequired = holderNameRequired
            return this
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * @param showStorePaymentField [Boolean]
         * @return [CardConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            builderIsStorePaymentFieldVisible = showStorePaymentField
            return this
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the [com.adyen.checkout.components.model.payments.request.PaymentComponentData]
         * for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return [CardConfiguration.Builder]
         */
        fun setShopperReference(shopperReference: String): Builder {
            this.builderShopperReference = shopperReference
            return this
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a regular payment.
         * Note that this might have implications for the risk of the transaction. Talk to Adyen Support before enabling this.
         *
         * @param hideCvc If CVC should be hidden or not.
         * @return [CardConfiguration.Builder]
         */
        fun setHideCvc(hideCvc: Boolean): Builder {
            builderHideCvc = hideCvc
            return this
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a stored payment flow.
         * Note that this has implications for the risk of the transaction. Talk to Adyen Support before enabling this.
         *
         * @param hideCvcStoredCard If CVC should be hidden or not for stored payments.
         * @return [CardConfiguration.Builder]
         */
        fun setHideCvcStoredCard(hideCvcStoredCard: Boolean): Builder {
            builderHideCvcStoredCard = hideCvcStoredCard
            return this
        }

        /**
         * Set if CPF/CNPJ field for Brazil merchants should be visible or not.
         *
         * @param socialSecurityNumberVisibility If CPF/CNPJ field should be visible or not.
         * @return [CardConfiguration.Builder]
         */
        fun setSocialSecurityNumberVisibility(socialSecurityNumberVisibility: SocialSecurityNumberVisibility): Builder {
            builderSocialSecurityNumberVisibility = socialSecurityNumberVisibility
            return this
        }

        fun setKcpAuthVisibility(kcpAuthVisibility: KCPAuthVisibility): Builder {
            builderKcpAuthVisibility = kcpAuthVisibility
            return this
        }

        /**
         * Specifies whether address input fields should be shown or not and in which form.
         *
         * @param addressVisibility The visibility state of the address input fields.
         * @return [CardConfiguration.Builder]
         */
        @Deprecated(
            message = "In favor of setAddressConfiguration(AddressConfiguration). Full address " +
                "form is only supported through using setAddressConfiguration(AddressConfiguration)."
        )
        fun setAddressVisibility(addressVisibility: AddressVisibility): Builder {
            builderAddressVisibility = addressVisibility
            return this
        }

        /**
         * Configures the installment options to be provided to the shopper.
         *
         * @param installmentConfiguration The configuration object for installment options.
         * @return [CardConfiguration.Builder]
         */
        fun setInstallmentConfigurations(installmentConfiguration: InstallmentConfiguration): Builder {
            builderInstallmentConfiguration = installmentConfiguration
            return this
        }

        /**
         * Configures the address form to be shown to the shopper.
         *
         * @param addressConfiguration The configuration object for address form.
         * @return [CardConfiguration.Builder]
         */
        fun setAddressConfiguration(addressConfiguration: AddressConfiguration): Builder {
            builderAddressConfiguration = addressConfiguration
            return this
        }

        /**
         * Build [CardConfiguration] object from [CardConfiguration.Builder] inputs.
         *
         * @return [CardConfiguration]
         */
        override fun buildInternal(): CardConfiguration {
            return CardConfiguration(
                shopperLocale = builderShopperLocale,
                environment = builderEnvironment,
                clientKey = builderClientKey,
                isHolderNameRequired = builderHolderNameRequired,
                supportedCardTypes = builderSupportedCardTypes,
                shopperReference = builderShopperReference,
                isStorePaymentFieldVisible = builderIsStorePaymentFieldVisible,
                isHideCvc = builderHideCvc,
                isHideCvcStoredCard = builderHideCvcStoredCard,
                socialSecurityNumberVisibility = builderSocialSecurityNumberVisibility,
                kcpAuthVisibility = builderKcpAuthVisibility,
                addressVisibility = builderAddressVisibility,
                installmentConfiguration = builderInstallmentConfiguration,
                addressConfiguration = builderAddressConfiguration,
            )
        }
    }

    companion object {
        val DEFAULT_SUPPORTED_CARDS_LIST: List<CardType> = listOf(
            CardType.VISA,
            CardType.AMERICAN_EXPRESS,
            CardType.MASTERCARD
        )

        @JvmField
        val CREATOR: Parcelable.Creator<CardConfiguration> = object : Parcelable.Creator<CardConfiguration> {
            override fun createFromParcel(parcel: Parcel): CardConfiguration {
                return CardConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<CardConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
