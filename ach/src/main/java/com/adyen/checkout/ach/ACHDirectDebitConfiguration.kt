/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 24/1/2023.
 */

package com.adyen.checkout.ach

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentMethodTypes
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.components.core.internal.util.CheckoutConfigurationMarker
import com.adyen.checkout.core.old.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [ACHDirectDebitComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class ACHDirectDebitConfiguration private constructor(
    override val shopperLocale: Locale?,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    internal val genericActionConfiguration: GenericActionConfiguration,
    val addressConfiguration: ACHDirectDebitAddressConfiguration?,
    val isStorePaymentFieldVisible: Boolean?,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create an [ACHDirectDebitConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<ACHDirectDebitConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isSubmitButtonVisible: Boolean? = null
        private var addressConfiguration: ACHDirectDebitAddressConfiguration? = null
        private var isStorePaymentFieldVisible: Boolean? = null

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
         * Initialize a configuration builder with the required fields and a shopper locale.
         *
         * @param shopperLocale The [Locale] of the shopper.
         * @param environment The [Environment] to be used for internal network calls from the SDK to Adyen.
         * @param clientKey Your Client Key used for internal network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey,
        )

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
         * Configures the address form to be shown to the shopper.
         *
         * Default is [ACHDirectDebitAddressConfiguration.FullAddress].
         * Default supported countries are ["US", "PR"]
         * @param addressConfiguration The configuration object for address form.
         * @return [ACHDirectDebitConfiguration.Builder]
         */
        fun setAddressConfiguration(addressConfiguration: ACHDirectDebitAddressConfiguration): Builder {
            this.addressConfiguration = addressConfiguration
            return this
        }

        /**
         * Set if the option to store the ACH Direct Debit for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * Not applicable for the sessions flow. Check out the
         * [Sessions API documentation](https://docs.adyen.com/api-explorer/Checkout/latest/post/sessions) on how to set
         * this value.
         *
         * @param showStorePaymentField [Boolean]
         * @return [ACHDirectDebitConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            isStorePaymentFieldVisible = showStorePaymentField
            return this
        }

        override fun buildInternal(): ACHDirectDebitConfiguration {
            return ACHDirectDebitConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isSubmitButtonVisible = isSubmitButtonVisible,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
                addressConfiguration = addressConfiguration,
                isStorePaymentFieldVisible = isStorePaymentFieldVisible,
            )
        }
    }
}

fun CheckoutConfiguration.achDirectDebit(
    configuration: @CheckoutConfigurationMarker ACHDirectDebitConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = ACHDirectDebitConfiguration.Builder(environment, clientKey)
        .apply {
            shopperLocale?.let { setShopperLocale(it) }
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(PaymentMethodTypes.ACH, config)
    return this
}

internal fun CheckoutConfiguration.getACHDirectDebitConfiguration(): ACHDirectDebitConfiguration? {
    return getConfiguration(PaymentMethodTypes.ACH)
}

internal fun ACHDirectDebitConfiguration.toCheckoutConfiguration(): CheckoutConfiguration {
    return CheckoutConfiguration(environment, clientKey, shopperLocale, amount, analyticsConfiguration) {
        addConfiguration(PaymentMethodTypes.ACH, this@toCheckoutConfiguration)

        genericActionConfiguration.getAllConfigurations().forEach {
            addActionConfiguration(it)
        }
    }
}
