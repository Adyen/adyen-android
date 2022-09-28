/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 27/9/2022.
 */

package com.adyen.checkout.paybybank

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import java.util.Locale

class PayByBankConfiguration : IssuerListConfiguration {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PayByBankConfiguration?> = object : Parcelable.Creator<PayByBankConfiguration?> {
            override fun createFromParcel(source: Parcel?): PayByBankConfiguration? {
                if (source == null) return null
                return PayByBankConfiguration(source)
            }

            override fun newArray(size: Int): Array<PayByBankConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }

    internal constructor(builder: Builder) : super(
        builder.builderShopperLocale,
        builder.builderEnvironment,
        builder.builderClientKey
    )

    internal constructor(parcel: Parcel) : super(parcel)

    /**
     * Builder to create a [PayByBankConfiguration].
     */
    class Builder : BaseConfigurationBuilder<PayByBankConfiguration> {
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
        constructor(configuration: PayByBankConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): PayByBankConfiguration {
            return PayByBankConfiguration(this)
        }
    }
}
