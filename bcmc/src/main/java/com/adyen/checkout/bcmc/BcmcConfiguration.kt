/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 18/9/2019.
 */
package com.adyen.checkout.bcmc

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.util.ParcelUtils.readBoolean
import com.adyen.checkout.core.util.ParcelUtils.writeBoolean
import java.util.Locale

/**
 * [Configuration] class required by [BcmcComponent] to change it's behavior. Pass it to the [BcmcComponent.PROVIDER].
 */
class BcmcConfiguration : Configuration {

    val shopperReference: String?
    val isStorePaymentFieldVisible: Boolean

    internal constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        shopperReference: String?,
        isStorePaymentFieldVisible: Boolean,
    ) : super(shopperLocale, environment, clientKey) {
        this.shopperReference = shopperReference
        this.isStorePaymentFieldVisible = isStorePaymentFieldVisible
    }

    internal constructor(parcel: Parcel) : super(parcel) {
        shopperReference = parcel.readString()
        isStorePaymentFieldVisible = readBoolean(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(shopperReference)
        writeBoolean(parcel, isStorePaymentFieldVisible)
    }

    /**
     * Builder to create a [BcmcConfiguration].
     */
    class Builder : BaseConfigurationBuilder<BcmcConfiguration> {
        private var builderShowStorePaymentField = false
        private var builderShopperReference: String? = null

        /**
         * Constructor of Card Configuration Builder with default values.
         *
         * @param context   A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, clientKey: String) : super(context, clientKey)

        /**
         * Builder with required parameters for a [BcmcConfiguration].
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
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: BcmcConfiguration) : super(configuration) {
            builderShopperReference = configuration.shopperReference
            builderShowStorePaymentField = configuration.isStorePaymentFieldVisible
        }

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        /**
         * Set if the option to store the card for future payments should be shown as an input field.
         *
         * @param showStorePaymentField [Boolean]
         * @return [BcmcConfiguration.Builder]
         */
        fun setShowStorePaymentField(showStorePaymentField: Boolean): Builder {
            builderShowStorePaymentField = showStorePaymentField
            return this
        }

        /**
         * Set the unique reference for the shopper doing this transaction.
         * This value will simply be passed back to you in the [com.adyen.checkout.components.model.payments.request.PaymentComponentData]
         * for convenience.
         *
         * @param shopperReference The unique shopper reference
         * @return [BcmcConfiguration.Builder]
         */
        fun setShopperReference(shopperReference: String): Builder {
            builderShopperReference = shopperReference
            return this
        }

        /**
         * Build [BcmcConfiguration] object from [BcmcConfiguration.Builder] inputs.
         *
         * @return [BcmcConfiguration]
         */
        override fun buildInternal(): BcmcConfiguration {
            return BcmcConfiguration(
                shopperLocale = builderShopperLocale,
                environment = builderEnvironment,
                clientKey = builderClientKey,
                shopperReference = builderShopperReference,
                isStorePaymentFieldVisible = builderShowStorePaymentField,
            )
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BcmcConfiguration> = object : Parcelable.Creator<BcmcConfiguration> {
            override fun createFromParcel(parcel: Parcel): BcmcConfiguration {
                return BcmcConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<BcmcConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
