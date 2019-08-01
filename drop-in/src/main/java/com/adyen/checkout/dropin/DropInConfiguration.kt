/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/3/2019.
 */

package com.adyen.checkout.dropin

import android.content.ComponentName
import android.content.Context
import android.util.DisplayMetrics
import com.adyen.checkout.base.Configuration
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dropin.DropInConfiguration.Builder
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import java.util.* // ktlint-disable no-wildcard-imports
import kotlin.collections.HashMap

/**
 * This is the base configuration for the Drop-In solution. You need to use the [Builder] to instantiate this class.
 * There you will find specific methods to add configurations for each specific PaymentComponent, to be able to customize their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
class DropInConfiguration internal constructor(builder: Builder) : Configuration {

    private val availableConfigs = builder.availableConfigs

    private val shopperLocale = builder.shopperLocale
    val displayMetrics = builder.displayMetrics
    private val environment: Environment = builder.mEnvironment
    val serviceComponentName: ComponentName = builder.serviceComponentName

    override fun getShopperLocale(): Locale {
        return shopperLocale
    }

    override fun getEnvironment(): Environment {
        return environment
    }

    fun <T : Configuration> getConfigurationFor(@PaymentMethodTypes.SupportedPaymentMethod paymentMethod: String, context: Context): T {
        return if (PaymentMethodTypes.SUPPORTED_PAYMENT_METHODS.contains(paymentMethod) && availableConfigs.containsKey(paymentMethod)) {
            @Suppress("UNCHECKED_CAST")
            availableConfigs[paymentMethod] as T
        } else {
            getDefaultConfigFor(paymentMethod, context)
        }
    }

    /**
     * Builder for creating a [DropInConfiguration] where you can set specific Configurations for a Payment Method
     */
    class Builder(context: Context, serviceClass: Class<out Any?>) {

        companion object {
            val TAG = LogUtil.getTag()
        }

        val serviceComponentName: ComponentName
        val shopperLocale: Locale
        val displayMetrics: DisplayMetrics
        val mEnvironment: Environment = Environment.EUROPE

        internal val availableConfigs = HashMap<String, Configuration>()

        init {
            Logger.d(TAG, "class name - ${serviceClass.name}")
            serviceComponentName = ComponentName(context.packageName, serviceClass.name)
            shopperLocale = LocaleUtil.getLocale(context)
            displayMetrics = context.resources.displayMetrics
        }

        fun build(): DropInConfiguration {
            return DropInConfiguration(this)
        }

        /**
         * Add configuration for Credit Card payment method.
         */
        fun addCardConfiguration(cardConfiguration: CardConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.SCHEME] = cardConfiguration
            return this
        }

        /**
         * Add configuration for iDeal payment method.
         */
        fun addIdealConfiguration(idealConfiguration: IdealConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.IDEAL] = idealConfiguration
            return this
        }

        /**
         * Add configuration for MolPay payment method.
         */
        fun addMolpayConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.MOLPAY] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for DotPay payment method.
         */
        fun addDotpayConfiguration(dotpayConfiguration: DotpayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.DOTPAY] = dotpayConfiguration
            return this
        }

        /**
         * Add configuration for EPS payment method.
         */
        fun addEpsConfiguration(epsConfiguration: EPSConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.EPS] = epsConfiguration
            return this
        }

        /**
         * Add configuration for EnterCash payment method.
         */
        fun addEntercashConfiguration(entercashConfiguration: EntercashConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.ENTERCASH] = entercashConfiguration
            return this
        }

        /**
         * Add configuration for Open Banking payment method.
         */
        fun addOpenBankingConfiguration(openBankingConfiguration: OpenBankingConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.OPEN_BANKING] = openBankingConfiguration
            return this
        }
    }
}
