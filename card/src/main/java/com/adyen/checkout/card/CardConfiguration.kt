/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */
package com.adyen.checkout.card

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [CardComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class CardConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val isHolderNameRequired: Boolean?,
    val supportedCardBrands: List<CardBrand>?,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean?,
    val isHideCvc: Boolean?,
    val isHideCvcStoredCard: Boolean?,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility?,
    val kcpAuthVisibility: KCPAuthVisibility?,
    val installmentConfiguration: InstallmentConfiguration?,
    val addressConfiguration: AddressConfiguration?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [CardConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<CardConfiguration, Builder>,
        ButtonConfigurationBuilder {
        private var supportedCardBrands: List<CardBrand>? = null
        private var holderNameRequired: Boolean? = null
        private var isStorePaymentFieldVisible: Boolean? = null
        private var shopperReference: String? = null
        private var isHideCvc: Boolean? = null
        private var isHideCvcStoredCard: Boolean? = null
        private var isSubmitButtonVisible: Boolean? = null
        private var socialSecurityNumberVisibility: SocialSecurityNumberVisibility? = null
        private var kcpAuthVisibility: KCPAuthVisibility? = null
        private var installmentConfiguration: InstallmentConfiguration? = null
        private var addressConfiguration: AddressConfiguration? = null

        /**
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A Context
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey,
        )

        /**
         * Builder with parameters for a [CardConfiguration].
         *
         * @param shopperLocale The [Locale] of the shopper.
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        /**
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * Defaults to [PaymentMethod.brands] if it exists, or [DEFAULT_SUPPORTED_CARDS_LIST] otherwise.
         *
         * Use this method when adding supported types that are not inside the [CardType] enum.
         *
         * @param supportCardBrands array of [CardBrand]
         * @return [CardConfiguration.Builder]
         */
        fun setSupportedCardTypes(vararg supportCardBrands: CardBrand): Builder {
            supportedCardBrands = listOf(*supportCardBrands)
            return this
        }

        /**
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * Defaults to [PaymentMethod.brands] if it exists, or [DEFAULT_SUPPORTED_CARDS_LIST] otherwise.
         *
         * @param supportCardTypes array of [CardType]
         * @return [CardConfiguration.Builder]
         */
        fun setSupportedCardTypes(vararg supportCardTypes: CardType): Builder {
            supportedCardBrands = listOf(*supportCardTypes).map { CardBrand(cardType = it) }
            return this
        }

        /**
         * Set if the holder name is required and should be shown as an input field.
         *
         * Default is false.
         *
         * @param holderNameRequired [Boolean]
         * @return [CardConfiguration.Builder]
         */
        fun setHolderNameRequired(holderNameRequired: Boolean): Builder {
            this.holderNameRequired = holderNameRequired
            return this
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * When using `sessions` show store payment field will be ignored and replaced with the value
         * sent to `/sessions` call.
         *
         * @param showStorePaymentField [Boolean]
         * @return [CardConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            isStorePaymentFieldVisible = showStorePaymentField
            return this
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the [PaymentComponentData] for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return [CardConfiguration.Builder]
         */
        fun setShopperReference(shopperReference: String): Builder {
            this.shopperReference = shopperReference
            return this
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a regular
         * payment.
         * Note that this might have implications for the risk of the transaction. Talk to Adyen Support before enabling
         * this.
         *
         * Default is false.
         *
         * @param hideCvc If CVC should be hidden or not.
         * @return [CardConfiguration.Builder]
         */
        fun setHideCvc(hideCvc: Boolean): Builder {
            this.isHideCvc = hideCvc
            return this
        }

        /**
         * Set if the CVC field should be hidden from the Component and not requested to the shopper on a stored payment
         * flow.
         * Note that this has implications for the risk of the transaction. Talk to Adyen Support before enabling this.
         *
         * Default is false.
         *
         * @param hideCvcStoredCard If CVC should be hidden or not for stored payments.
         * @return [CardConfiguration.Builder]
         */
        fun setHideCvcStoredCard(hideCvcStoredCard: Boolean): Builder {
            isHideCvcStoredCard = hideCvcStoredCard
            return this
        }

        /**
         * Set if CPF/CNPJ field for Brazil merchants should be visible or not.
         *
         * Default is [SocialSecurityNumberVisibility.HIDE].
         *
         * @param socialSecurityNumberVisibility If CPF/CNPJ field should be visible or not.
         * @return [CardConfiguration.Builder]
         */
        fun setSocialSecurityNumberVisibility(socialSecurityNumberVisibility: SocialSecurityNumberVisibility): Builder {
            this.socialSecurityNumberVisibility = socialSecurityNumberVisibility
            return this
        }

        /**
         * Set if security fields for Korean cards should be visible or not.
         *
         * Default is [KCPAuthVisibility.HIDE].
         *
         * @param kcpAuthVisibility If security fields for Korean cards should be visible or not.
         * @return [CardConfiguration.Builder]
         */
        fun setKcpAuthVisibility(kcpAuthVisibility: KCPAuthVisibility): Builder {
            this.kcpAuthVisibility = kcpAuthVisibility
            return this
        }

        /**
         * Configures the installment options to be provided to the shopper.
         *
         * When using `sessions` installment configuration will be ignored and replaced with the value
         * sent to `/sessions` call.
         *
         * @param installmentConfiguration The configuration object for installment options.
         * @return [CardConfiguration.Builder]
         */
        fun setInstallmentConfigurations(installmentConfiguration: InstallmentConfiguration): Builder {
            this.installmentConfiguration = installmentConfiguration
            return this
        }

        /**
         * Configures the address form to be shown to the shopper.
         *
         * Default is [AddressConfiguration.None].
         *
         * @param addressConfiguration The configuration object for address form.
         * @return [CardConfiguration.Builder]
         */
        fun setAddressConfiguration(addressConfiguration: AddressConfiguration): Builder {
            this.addressConfiguration = addressConfiguration
            return this
        }

        /**
         * Sets if submit button will be visible or not.
         *
         * Default is True.
         *
         * @param isSubmitButtonVisible Is submit button should be visible or not.
         */
        override fun setSubmitButtonVisible(isSubmitButtonVisible: Boolean): Builder {
            this.isSubmitButtonVisible = isSubmitButtonVisible
            return this
        }

        /**
         * Build [CardConfiguration] object from [CardConfiguration.Builder] inputs.
         *
         * @return [CardConfiguration]
         */
        override fun buildInternal(): CardConfiguration {
            return CardConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isHolderNameRequired = holderNameRequired,
                isSubmitButtonVisible = isSubmitButtonVisible,
                supportedCardBrands = supportedCardBrands,
                shopperReference = shopperReference,
                isStorePaymentFieldVisible = isStorePaymentFieldVisible,
                isHideCvc = isHideCvc,
                isHideCvcStoredCard = isHideCvcStoredCard,
                socialSecurityNumberVisibility = socialSecurityNumberVisibility,
                kcpAuthVisibility = kcpAuthVisibility,
                installmentConfiguration = installmentConfiguration,
                addressConfiguration = addressConfiguration,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }

    companion object {
        val DEFAULT_SUPPORTED_CARDS_LIST: List<CardBrand> = listOf(
            CardBrand(cardType = CardType.VISA),
            CardBrand(cardType = CardType.AMERICAN_EXPRESS),
            CardBrand(cardType = CardType.MASTERCARD),
        )
    }
}

fun CheckoutConfiguration.card(
    configuration: @CheckoutConfigurationMarker CardConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = CardConfiguration.Builder(shopperLocale, environment, clientKey)
        .apply {
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.SCHEME, config)
    return this
}

fun CheckoutConfiguration.getCardConfiguration(): CardConfiguration? {
    return getConfiguration(PaymentMethodTypes.SCHEME)
}

internal fun CardConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
    ) {
        addConfiguration(PaymentMethodTypes.SCHEME, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
