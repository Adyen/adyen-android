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
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.bcmc.BcmcConfiguration
import com.adyen.checkout.blik.BlikConfiguration
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.components.util.PaymentMethodTypes
import com.adyen.checkout.components.util.ValidationUtils
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.log.LogUtil
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.util.LocaleUtil
import com.adyen.checkout.core.util.ParcelUtils
import com.adyen.checkout.dotpay.DotpayConfiguration
import com.adyen.checkout.dropin.DropInConfiguration.Builder
import com.adyen.checkout.entercash.EntercashConfiguration
import com.adyen.checkout.eps.EPSConfiguration
import com.adyen.checkout.googlepay.GooglePayConfiguration
import com.adyen.checkout.ideal.IdealConfiguration
import com.adyen.checkout.mbway.MBWayConfiguration
import com.adyen.checkout.molpay.MolpayConfiguration
import com.adyen.checkout.openbanking.OpenBankingConfiguration
import com.adyen.checkout.sepa.SepaConfiguration
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set

/**
 * This is the base configuration for the Drop-In solution. You need to use the [Builder] to instantiate this class.
 * There you will find specific methods to add configurations for each specific PaymentComponent, to be able to customize their behavior.
 * If you don't specify anything, a default configuration will be used.
 */
@SuppressWarnings("TooManyFunctions")
class DropInConfiguration : Configuration, Parcelable {

    private val availableConfigs: HashMap<String, Configuration>
    val serviceComponentName: ComponentName
    val amount: Amount

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<DropInConfiguration> {
            override fun createFromParcel(parcel: Parcel) = DropInConfiguration(parcel)
            override fun newArray(size: Int) = arrayOfNulls<DropInConfiguration>(size)
        }
    }

    @Suppress("LongParameterList")
    constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        availableConfigs: HashMap<String, Configuration>,
        serviceComponentName: ComponentName,
        amount: Amount
    ) : super(shopperLocale, environment, clientKey) {
        this.availableConfigs = availableConfigs
        this.serviceComponentName = serviceComponentName
        this.amount = amount
    }

    constructor(parcel: Parcel) : super(parcel) {
        @Suppress("UNCHECKED_CAST")
        availableConfigs = parcel.readHashMap(Configuration::class.java.classLoader) as HashMap<String, Configuration>
        serviceComponentName = parcel.readParcelable(ComponentName::class.java.classLoader)!!
        amount = Amount.CREATOR.createFromParcel(parcel)
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeMap(availableConfigs)
        dest.writeParcelable(serviceComponentName, flags)
        JsonUtils.writeToParcel(dest, Amount.SERIALIZER.serialize(amount))
    }

    override fun describeContents(): Int {
        return ParcelUtils.NO_FILE_DESCRIPTOR
    }

    internal fun <T : Configuration> getConfigurationForPaymentMethod(paymentMethod: String, context: Context): T {
        return if (availableConfigs.containsKey(paymentMethod)) {
            @Suppress("UNCHECKED_CAST")
            availableConfigs[paymentMethod] as T
        } else {
            getDefaultConfigForPaymentMethod(paymentMethod, context, this)
        }
    }

    internal fun <T : Configuration> getConfigurationForAction(action: String, context: Context): T {
        // TODO fetch from availableConfigs after we support action configs
        return getDefaultConfigForAction(action, context, this)
    }

    /**
     * Builder for creating a [DropInConfiguration] where you can set specific Configurations for a Payment Method
     */
    class Builder {

        companion object {
            val TAG = LogUtil.getTag()
        }

        private val availableConfigs = HashMap<String, Configuration>()

        private var shopperLocale: Locale
        private var environment: Environment = Environment.EUROPE
        private var clientKey: String
        private var serviceComponentName: ComponentName
        private var amount: Amount = Amount.EMPTY

        private val packageName: String
        private val serviceClassName: String

        /**
         *
         * Create a [DropInConfiguration]
         *
         * @param context
         * @param serviceClass Service that extended from [DropInService] that would handle network requests.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, serviceClass: Class<out Any?>, clientKey: String) {
            this.packageName = context.packageName
            this.serviceClassName = serviceClass.name

            this.serviceComponentName = ComponentName(packageName, serviceClassName)
            this.shopperLocale = LocaleUtil.getLocale(context)

            if (!ValidationUtils.isClientKeyValid(clientKey)) {
                throw CheckoutException("Client key is not valid.")
            }
            this.clientKey = clientKey
        }

        /**
         * Create a Builder with the same values of an existing Configuration object.
         */
        constructor(dropInConfiguration: DropInConfiguration) {
            this.packageName = dropInConfiguration.serviceComponentName.packageName
            this.serviceClassName = dropInConfiguration.serviceComponentName.className

            this.serviceComponentName = dropInConfiguration.serviceComponentName
            this.shopperLocale = dropInConfiguration.shopperLocale
            this.environment = dropInConfiguration.environment
            this.amount = dropInConfiguration.amount
            this.clientKey = dropInConfiguration.clientKey
        }

        fun setServiceComponentName(serviceComponentName: ComponentName): Builder {
            this.serviceComponentName = serviceComponentName
            return this
        }

        /**
         * Sets the [Locale] to be used for localization on the Drop-in flow.<br>
         * Note that the [Locale] on the specific component configuration will still take priority and can cause inconsistency in the UI.<br>
         * Also, due to technical limitations, this Locale will be converted to String and lose additional variants other than language and country.
         */
        fun setShopperLocale(shopperLocale: Locale): Builder {
            this.shopperLocale = shopperLocale
            return this
        }

        fun setEnvironment(environment: Environment): Builder {
            this.environment = environment
            return this
        }

        fun setAmount(amount: Amount): Builder {
            if (!CheckoutCurrency.isSupported(amount.currency) || amount.value < 0) {
                throw CheckoutException("Currency is not valid.")
            }
            this.amount = amount
            return this
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
         * Add configuration for MolPay Thailand payment method.
         */
        fun addMolpayThailandConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.MOLPAY_THAILAND] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for MolPay Malasya payment method.
         */
        fun addMolpayMalasyaConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.MOLPAY_MALAYSIA] = molpayConfiguration
            return this
        }

        /**
         * Add configuration for MolPay Vietnam payment method.
         */
        fun addMolpayVietnamConfiguration(molpayConfiguration: MolpayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.MOLPAY_VIETNAM] = molpayConfiguration
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

        /**
         * Add configuration for Sepa payment method.
         */
        fun addSepaConfiguration(sepaConfiguration: SepaConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.SEPA] = sepaConfiguration
            return this
        }

        /**
         * Add configuration for BCMC payment method.
         */
        fun addBcmcConfiguration(bcmcConfiguration: BcmcConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.BCMC] = bcmcConfiguration
            return this
        }

        /**
         * Add configuration for MB WAY payment method.
         */
        fun addMBWayConfiguration(mbwayConfiguration: MBWayConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.MB_WAY] = mbwayConfiguration
            return this
        }

        /**
         * Add configuration for Blik payment method.
         */
        fun addBlikConfiguration(blikConfiguration: BlikConfiguration): Builder {
            availableConfigs[PaymentMethodTypes.BLIK] = blikConfiguration
            return this
        }

        /**
         * Create the [DropInConfiguration] instance.
         */
        fun build(): DropInConfiguration {
            return DropInConfiguration(
                shopperLocale,
                environment,
                clientKey,
                availableConfigs,
                serviceComponentName,
                amount
            )
        }
    }
}
