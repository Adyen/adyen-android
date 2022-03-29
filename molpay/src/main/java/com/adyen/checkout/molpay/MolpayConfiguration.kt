/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 11/6/2019.
 */
package com.adyen.checkout.molpay

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import java.util.Locale

class MolpayConfiguration : IssuerListConfiguration {

    internal constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
    ) : super(shopperLocale, environment, clientKey)

    internal constructor(parcel: Parcel) : super(parcel)

    /**
     * Builder to create a [MolpayConfiguration].
     */
    class Builder : IssuerListBuilder<MolpayConfiguration> {
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
        constructor(configuration: MolpayConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): MolpayConfiguration {
            return MolpayConfiguration(builderShopperLocale, builderEnvironment, builderClientKey)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MolpayConfiguration> = object : Parcelable.Creator<MolpayConfiguration> {
            override fun createFromParcel(parcel: Parcel): MolpayConfiguration {
                return MolpayConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<MolpayConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
