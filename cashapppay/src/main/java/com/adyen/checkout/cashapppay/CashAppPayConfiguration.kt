/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 6/3/2023.
 */

package com.adyen.checkout.cashapppay

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.AmountConfiguration
import com.adyen.checkout.components.base.AmountConfigurationBuilder
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.components.model.payments.Amount
import com.adyen.checkout.components.util.CheckoutCurrency
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.model.JsonUtils
import com.adyen.checkout.core.util.ParcelUtils
import java.util.Locale

class CashAppPayConfiguration : Configuration, AmountConfiguration {

    val cashAppPayEnvironment: CashAppPayEnvironment
    override val amount: Amount
    val returnUrl: String?
    val showStorePaymentField: Boolean

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<CashAppPayConfiguration?> = object : Parcelable.Creator<CashAppPayConfiguration?> {
            override fun createFromParcel(source: Parcel?): CashAppPayConfiguration? {
                if (source == null) return null
                return CashAppPayConfiguration(source)
            }

            override fun newArray(size: Int): Array<CashAppPayConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }

    internal constructor(builder: Builder) : super(builder.builderShopperLocale, builder.builderEnvironment, builder.builderClientKey) {
        cashAppPayEnvironment = builder.cashAppPayEnvironment
        amount = builder.amount
        returnUrl = builder.returnUrl
        showStorePaymentField = builder.showStorePaymentField
    }

    internal constructor(parcel: Parcel) : super(parcel) {
        cashAppPayEnvironment = CashAppPayEnvironment.valueOf(requireNotNull(parcel.readString()))
        amount = Amount.CREATOR.createFromParcel(parcel)
        returnUrl = parcel.readString()
        showStorePaymentField = ParcelUtils.readBoolean(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(cashAppPayEnvironment.name)
        JsonUtils.writeToParcel(parcel, Amount.SERIALIZER.serialize(amount))
        parcel.writeString(returnUrl)
        ParcelUtils.writeBoolean(parcel, showStorePaymentField)
    }

    /**
     * Builder to create a [CashAppPayConfiguration].
     */
    class Builder : BaseConfigurationBuilder<CashAppPayConfiguration>, AmountConfigurationBuilder {

        internal var cashAppPayEnvironment: CashAppPayEnvironment = getDefaultCashAppPayEnvironment(builderEnvironment)
            private set
        internal var amount: Amount = Amount.EMPTY
            private set
        internal var returnUrl: String? = null
            private set
        internal var showStorePaymentField: Boolean = true
            private set

        private var isCashAppPayEnvironmentSetManually = false

        private fun getDefaultCashAppPayEnvironment(environment: Environment): CashAppPayEnvironment {
            return if (environment == Environment.TEST) CashAppPayEnvironment.SANDBOX
            else CashAppPayEnvironment.PRODUCTION
        }

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, clientKey: String) : super(context, clientKey)

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(shopperLocale, environment, clientKey)

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: CashAppPayConfiguration) : super(configuration) {
            cashAppPayEnvironment = configuration.cashAppPayEnvironment
            amount = configuration.amount
            returnUrl = configuration.returnUrl
            showStorePaymentField = configuration.showStorePaymentField
        }

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            if (!isCashAppPayEnvironmentSetManually) {
                cashAppPayEnvironment = getDefaultCashAppPayEnvironment(builderEnvironment)
            }
            return super.setEnvironment(builderEnvironment) as Builder
        }

        /**
         * Sets the environment to be used by Cash App Pay.
         *
         * If not set, it will match the Adyen environment.
         *
         * @param cashAppPayEnvironment The Cash App Pay environment.
         */
        fun setCashAppPayEnvironment(cashAppPayEnvironment: CashAppPayEnvironment): Builder {
            isCashAppPayEnvironmentSetManually = true
            this.cashAppPayEnvironment = cashAppPayEnvironment

            return this
        }

        /**
         * Sets the required amount to launch Cash App Pay.
         *
         * The only supported currency is [CheckoutCurrency.USD].
         *
         * @param amount the amount of the transaction
         */
        override fun setAmount(amount: Amount): Builder {
            if (!CheckoutCurrency.isSupported(amount.currency) || amount.value < 0) {
                throw CheckoutException("Currency is not valid.")
            }
            this.amount = amount
            return this
        }

        /**
         *
         * Sets the required return URL that Cash App Pay will redirect to at the end of the transaction.
         *
         * @param returnUrl The Cash App Pay environment.
         */
        fun setReturnUrl(returnUrl: String): Builder {
            this.returnUrl = returnUrl
            return this
        }

        /**
         * Set if the option to store the shopper's account for future payments should be shown as an input field.
         *
         * Default is true.
         *
         * @param showStorePaymentField [Boolean]
         * @return [CashAppPayConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            this.showStorePaymentField = showStorePaymentField
            return this
        }

        override fun buildInternal(): CashAppPayConfiguration {
            return CashAppPayConfiguration(this)
        }
    }
}
