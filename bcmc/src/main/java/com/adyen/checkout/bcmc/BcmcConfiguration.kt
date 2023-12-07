/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import android.content.Context
import com.adyen.checkout.action.core.GenericActionConfiguration
import com.adyen.checkout.action.core.internal.ActionHandlingPaymentMethodConfigurationBuilder
import com.adyen.checkout.components.core.Amount
import com.adyen.checkout.components.core.AnalyticsConfiguration
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.PaymentComponentData
import com.adyen.checkout.components.core.internal.ButtonConfiguration
import com.adyen.checkout.components.core.internal.ButtonConfigurationBuilder
import com.adyen.checkout.components.core.internal.Configuration
import com.adyen.checkout.core.Environment
import kotlinx.parcelize.Parcelize
import java.util.Locale

/**
 * Configuration class for the [BcmcComponent].
 */
@Parcelize
@Suppress("LongParameterList")
class BcmcConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val analyticsConfiguration: AnalyticsConfiguration?,
    override val amount: Amount?,
    override val isSubmitButtonVisible: Boolean?,
    val isHolderNameRequired: Boolean?,
    val shopperReference: String?,
    val isStorePaymentFieldVisible: Boolean?,
    internal val genericActionConfiguration: GenericActionConfiguration,
) : Configuration, ButtonConfiguration {

    /**
     * Builder to create a [BcmcConfiguration].
     */
    class Builder :
        ActionHandlingPaymentMethodConfigurationBuilder<BcmcConfiguration, Builder>,
        ButtonConfigurationBuilder {

        private var isHolderNameRequired: Boolean? = null
        private var showStorePaymentField: Boolean? = null
        private var shopperReference: String? = null
        private var isSubmitButtonVisible: Boolean? = null

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
         * Builder with required parameters for a [BcmcConfiguration].
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
         * Set if the holder name is required and should be shown as an input field.
         *
         * Default is false.
         *
         * @param isHolderNameRequired [Boolean]
         * @return [BcmcConfiguration.Builder]
         */
        fun setHolderNameRequired(isHolderNameRequired: Boolean): Builder {
            this.isHolderNameRequired = isHolderNameRequired
            return this
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * Default is false.
         *
         * When using `sessions` show store payment field will be ignored and replaced with the value
         * sent to `/sessions` call.
         *
         * @param showStorePaymentField [Boolean]
         * @return [BcmcConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            this.showStorePaymentField = showStorePaymentField
            return this
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the [PaymentComponentData] for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return [BcmcConfiguration.Builder]
         */
        fun setShopperReference(shopperReference: String): Builder {
            this.shopperReference = shopperReference
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
         * Build [BcmcConfiguration] object from [BcmcConfiguration.Builder] inputs.
         *
         * @return [BcmcConfiguration]
         */
        override fun buildInternal(): BcmcConfiguration {
            return BcmcConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                analyticsConfiguration = analyticsConfiguration,
                amount = amount,
                isHolderNameRequired = isHolderNameRequired,
                shopperReference = shopperReference,
                isStorePaymentFieldVisible = showStorePaymentField,
                isSubmitButtonVisible = isSubmitButtonVisible,
                genericActionConfiguration = genericActionConfigurationBuilder.build(),
            )
        }
    }
}

fun CheckoutConfiguration.bcmcConfiguration(
    configuration: BcmcConfiguration.Builder.() -> Unit = {}
): CheckoutConfiguration {
    val config = BcmcConfiguration.Builder(shopperLocale, environment, clientKey)
        .apply {
            amount?.let { setAmount(it) }
            analyticsConfiguration?.let { setAnalyticsConfiguration(it) }
        }
        .apply(configuration)
        .build()
    addConfiguration(config)
    return this
}

fun CheckoutConfiguration.getBcmcConfiguration(): BcmcConfiguration? {
    return getConfiguration(BcmcConfiguration::class)
}
