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
import android.content.Intent
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.base.Configuration
import com.adyen.checkout.base.util.PaymentMethodTypes
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.log.Logger
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.core.util.ParcelUtils
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dropin.DropInConfiguration.Builder
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import java.util.Locale

/**
 * This is the base configuration for the Drop-In solution. You need to use the [Builder] to instantiate this class.
 * There you will find specific methods to add configurations for each specific PaymentComponent, to be able to customize their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
class DropInConfiguration : Configuration, Parcelable {

    private val availableConfigs: HashMap<String, Configuration>
    private val shopperLocale: Locale
    private val environment: Environment
    val serviceComponentName: ComponentName
    var resultHandlerIntent: Intent = Intent()

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DropInConfiguration> {
            override fun createFromParcel(parcel: Parcel) = DropInConfiguration(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DropInConfiguration>(size)
        }
    }

    constructor(builder: Builder) {
        availableConfigs = builder.availableConfigs
        shopperLocale = builder.shopperLocale
        environment = builder.mEnvironment
        serviceComponentName = builder.serviceComponentName
        builder.resultHandlerIntent?.let {
            resultHandlerIntent = it
        }
    }

    constructor(parcel: Parcel) {
        availableConfigs = parcel.readHashMap(Configuration::class.java.classLoader) as HashMap<String, Configuration>
        shopperLocale = parcel.readSerializable() as Locale
        environment = parcel.readParcelable(Environment::class.java.classLoader)
        serviceComponentName = parcel.readParcelable(ComponentName::class.java.classLoader)
        resultHandlerIntent = parcel.readParcelable(Intent::class.java.classLoader)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeMap(availableConfigs)
        dest.writeSerializable(shopperLocale)
        dest.writeParcelable(environment, flags)
        dest.writeParcelable(serviceComponentName, flags)
        dest.writeParcelable(resultHandlerIntent, flags)
    }

    override fun describeContents(): Int {
        return ParcelUtils.NO_FILE_DESCRIPTOR
    }

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
            getDefaultConfigFor(paymentMethod, context, this)
        }
    }

    /**
     * Builder for creating a [DropInConfiguration] where you can set specific Configurations for a Payment Method
     */
    class Builder {

        companion object {
            val TAG = LogUtil.getTag()
        }

        var serviceComponentName: ComponentName
        var shopperLocale: Locale
        var mEnvironment: Environment = Environment.EUROPE
        var resultHandlerIntent: Intent? = null

        private val packageName: String
        private val serviceClassName: String
        private val context: Context

        @Deprecated("You need to pass resultHandlerIntent to drop-in configuration")
        constructor(context: Context, serviceClass: Class<out Any?>) : this(context, null, serviceClass)

        /**
         * @param context
         * @param resultHandlerIntent The Intent used with [Activity.startActivity] that will contain the payment result extra with key [RESULT_KEY].
         * @param serviceClass Service that extended from [DropInService] that would handle network requests.
         */
        constructor(context: Context, resultHandlerIntent: Intent?, serviceClass: Class<out Any?>) {
            this.packageName = context.packageName
            this.serviceClassName = serviceClass.name
            this.resultHandlerIntent = resultHandlerIntent
            this.context = context

            Logger.d(TAG, "class name - $serviceClassName")
            serviceComponentName = ComponentName(packageName, serviceClassName)
            shopperLocale = LocaleUtil.getLocale(context)
        }

        internal val availableConfigs = HashMap<String, Configuration>()

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

        /**
         * Add configuration for Google Pay payment method.
         */
        fun addGooglePayConfiguration(googlePayConfiguration: GooglePayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.GOOGLE_PAY] = googlePayConfiguration
            return this
        }
    }
}
