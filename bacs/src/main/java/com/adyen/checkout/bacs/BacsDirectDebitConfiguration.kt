/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 2/11/2021.
 */

package com.adyen.checkout.bacs

import android.content.Context
import com.adyen.checkout.components.base.AmountConfiguration
import com.adyen.checkout.components.base.AmountConfigurationBuilder
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import kotlinx.parcelize.Parcelize
import java.util.Locale

@Parcelize
class BacsDirectDebitConfiguration private constructor(
    override val shopperLocale: Locale,
    override val environment: Environment,
    override val clientKey: String,
    override val amount: Amount?,
    override val isAnalyticsEnabled: Boolean?,
) : Configuration, AmountConfiguration {

    class Builder : BaseConfigurationBuilder<BacsDirectDebitConfiguration, Builder>, AmountConfigurationBuilder {

        internal var amount: Amount? = null

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
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(
            shopperLocale,
            environment,
            clientKey
        )

        override fun buildInternal(): BacsDirectDebitConfiguration {
            return BacsDirectDebitConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                amount = amount,
                isAnalyticsEnabled = isAnalyticsEnabled,
            )
        }

        override fun setAmount(amount: Amount): Builder {
            if (!CheckoutCurrency.isSupported(amount.currency) || amount.value < 0) {
                throw CheckoutException("Currency is not valid.")
            }
            this.amount = amount
            return this
        }
    }
}
