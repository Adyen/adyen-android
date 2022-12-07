/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ran on 14/3/2019.
 */
package com.adyen.checkout.card

import android.content.Context
import com.adyen.checkout.action.ActionHandlingConfigurationBuilder
import com.adyen.checkout.action.GenericActionConfiguration
import com.adyen.checkout.adyen3ds2.Adyen3DS2Configuration
import com.adyen.checkout.await.AwaitConfiguration
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.paymentmethods.PaymentMethod
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.qrcode.QRCodeConfiguration
import com.adyen.checkout.redirect.RedirectConfiguration
import com.adyen.checkout.voucher.VoucherConfiguration
import com.adyen.checkout.wechatpay.WeChatPayActionConfiguration
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * [Configuration] class required by [CardComponent] to change it's behavior. Pass it to the [CardComponent.PROVIDER].
 */
@Parcelize
@Suppress("LongParameterList")
class CardConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val isAnalyticsEnabled: Boolean?,
    val isHolderNameRequired: Boolean?,
    val supportedCardTypes: List<CardType>?,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean?,
    val isHideCvc: Boolean?,
    val isHideCvcStoredCard: Boolean?,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility?,
    val kcpAuthVisibility: KCPAuthVisibility?,
    val installmentConfiguration: InstallmentConfiguration?,
    val addressConfiguration: AddressConfiguration?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration {

    /**
     * Builder to create a [CardConfiguration].
     */
    @Suppress("TooManyFunctions")
    class Builder : BaseConfigurationBuilder<CardConfiguration, Builder>, ActionHandlingConfigurationBuilder {
        private var supportedCardTypes: List<CardType>? = null
        private var holderNameRequired: Boolean? = null
        private var isStorePaymentFieldVisible: Boolean? = null
        private var shopperReference: String? = null
        private var isHideCvc: Boolean? = null
        private var isHideCvcStoredCard: Boolean? = null
        private var socialSecurityNumberVisibility: SocialSecurityNumberVisibility? = null
        private var kcpAuthVisibility: KCPAuthVisibility? = null
        private var installmentConfiguration: InstallmentConfiguration? = null
        private var addressConfiguration: AddressConfiguration? = null

        private val genericActionConfigurationBuilder = GenericActionConfiguration.Builder(
            shopperLocale = shopperLocale,
            environment = environment,
            clientKey = clientKey,
        )

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

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

        /**
         * Set the supported card types for this payment. Supported types will be shown as user inputs the card number.
         *
         * Defaults to [PaymentMethod.brands] if it exists, or [DEFAULT_SUPPORTED_CARDS_LIST] otherwise.
         *
         * @param supportCardTypes array of [CardType]
         * @return [CardConfiguration.Builder]
         */
        fun setSupportedCardTypes(vararg supportCardTypes: CardType): Builder {
            supportedCardTypes = listOf(*supportCardTypes)
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
         * @param showStorePaymentField [Boolean]
         * @return [CardConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            isStorePaymentFieldVisible = showStorePaymentField
            return this
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the
         * [com.adyen.checkout.components.model.payments.request.PaymentComponentData] for convenience.
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
         * Add configuration for 3DS2 action.
         */
        override fun add3ds2ActionConfiguration(configuration: Adyen3DS2Configuration): Builder {
            genericActionConfigurationBuilder.add3ds2ActionConfiguration(configuration)
            return this
        }

        /**
         * Add configuration for Await action.
         */
        override fun addAwaitActionConfiguration(configuration: AwaitConfiguration): Builder {
            genericActionConfigurationBuilder.addAwaitActionConfiguration(configuration)
            return this
        }

        /**
         * Add configuration for QR code action.
         */
        override fun addQRCodeActionConfiguration(configuration: QRCodeConfiguration): Builder {
            genericActionConfigurationBuilder.addQRCodeActionConfiguration(configuration)
            return this
        }

        /**
         * Add configuration for Redirect action.
         */
        override fun addRedirectActionConfiguration(configuration: RedirectConfiguration): Builder {
            genericActionConfigurationBuilder.addRedirectActionConfiguration(configuration)
            return this
        }

        /**
         * Add configuration for Voucher action.
         */
        override fun addVoucherActionConfiguration(configuration: VoucherConfiguration): Builder {
            genericActionConfigurationBuilder.addVoucherActionConfiguration(configuration)
            return this
        }

        /**
         * Add configuration for WeChat Pay action.
         */
        override fun addWeChatPayActionConfiguration(configuration: WeChatPayActionConfiguration): Builder {
            genericActionConfigurationBuilder.addWeChatPayActionConfiguration(configuration)
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
                isAnalyticsEnabled = isAnalyticsEnabled,
                isHolderNameRequired = holderNameRequired,
                supportedCardTypes = supportedCardTypes,
                shopperReference = shopperReference,
                isStorePaymentFieldVisible = isStorePaymentFieldVisible,
                isHideCvc = isHideCvc,
                isHideCvcStoredCard = isHideCvcStoredCard,
                socialSecurityNumberVisibility = socialSecurityNumberVisibility,
                kcpAuthVisibility = kcpAuthVisibility,
                installmentConfiguration = installmentConfiguration,
                addressConfiguration = addressConfiguration,
                genericActionConfiguration = genericActionConfigurationBuilder.build()
            )
        }
    }

    companion object {
        val DEFAULT_SUPPORTED_CARDS_LIST: List<CardType> = listOf(
            CardType.VISA,
            CardType.AMERICAN_EXPRESS,
            CardType.MASTERCARD
        )
    }
}
