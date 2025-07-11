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
@Deprecated("Configuration classes are deprecated, use CheckoutConfiguration instead.")
class CardConfiguration private constructor(
    override val shopperLocale: Locale?,
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
    @Deprecated("Configuration builders are deprecated, use CheckoutConfiguration instead.")
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<CardConfiguration, Builder>,
        ButtonConfigurationBuilder {
        var supportedCardBrands: List<CardBrand>? = null
        var holderNameRequired: Boolean? = null
        var isStorePaymentFieldVisible: Boolean? = null
        var shopperReference: String? = null
        var isHideCvc: Boolean? = null
        var isHideCvcStoredCard: Boolean? = null
        var isSubmitButtonVisible: Boolean? = null
        var socialSecurityNumberVisibility: SocialSecurityNumberVisibility? = null
        var kcpAuthVisibility: KCPAuthVisibility? = null
        var installmentConfiguration: InstallmentConfiguration? = null
        var addressConfiguration: AddressConfiguration? = null

        /**
         * Initialize a configuration builder with the required fields.
         *
         * The shopper locale will match the value passed to the API with the sessions flow, or the primary user locale
         * on the device otherwise. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(environment: Environment, clientKey: String) : super(
            environment,
            clientKey,
        )

        /**
         * Alternative constructor that uses the [context] to fetch the user locale and use it as a shopper locale.
         *
         * @param context A Context
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        @Deprecated("You can omit the context parameter")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setHolderNameRequired(holderNameRequired: Boolean): Builder {
            this.holderNameRequired = holderNameRequired
            return this
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param showStorePaymentField [Boolean]
         * @return [CardConfiguration.Builder]
         */
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
        fun setKcpAuthVisibility(kcpAuthVisibility: KCPAuthVisibility): Builder {
            this.kcpAuthVisibility = kcpAuthVisibility
            return this
        }

        /**
         * Configures the installment options to be provided to the shopper.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param installmentConfiguration The configuration object for installment options.
         * @return [CardConfiguration.Builder]
         */
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Use property access syntax instead.")
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
        @Deprecated("Configure this in CheckoutConfiguration instead.")
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
    val config = CardConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
            isSubmitButtonVisible?.let { setSubmitButtonVisible(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.SCHEME, config)
    return this
}

internal fun CheckoutConfiguration.getCardConfiguration(): CardConfiguration? {
    return getConfiguration(PaymentMethodTypes.SCHEME)
}

internal fun CardConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(
        shopperLocale = shopperLocale,
        environment = environment,
        clientKey = clientKey,
        amount = amount,
        analyticsConfiguration = analyticsConfiguration,
        isSubmitButtonVisible = isSubmitButtonVisible,
    ) {
        addConfiguration(PaymentMethodTypes.SCHEME, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
