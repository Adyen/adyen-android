/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.entercash

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import java.util.Locale

class EntercashConfiguration : IssuerListConfiguration {

    /**
     * @param builder The Builder instance to create the configuration.
     */
    internal constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
    ) : super(shopperLocale, environment, clientKey)

    internal constructor(parcel: Parcel) : super(parcel)

    /**
     * Builder to create a [EntercashConfiguration].
     */
    class Builder : IssuerListBuilder<EntercashConfiguration> {
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
        constructor(configuration: EntercashConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): EntercashConfiguration {
            return EntercashConfiguration(
                shopperLocale = builderShopperLocale,
                environment = builderEnvironment,
                clientKey = builderClientKey,
            )
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EntercashConfiguration> = object : Parcelable.Creator<EntercashConfiguration> {
            override fun createFromParcel(parcel: Parcel): EntercashConfiguration {
                return EntercashConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<EntercashConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
