/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.adyen3ds2

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

class Adyen3DS2Configuration : Configuration {

    private constructor(builder: Builder) : super(
        builder.shopperLocale,
        builder.environment,
        builder.clientKey
    )

    private constructor(inputParcel: Parcel) : super(inputParcel)

    /**
     * Builder to create a [Adyen3DS2Configuration].
     */
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration> {

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

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: Adyen3DS2Configuration) : super(configuration)

        override fun setShopperLocale(shopperLocale: Locale): Builder {
            return super.setShopperLocale(shopperLocale) as Builder
        }

        override fun setEnvironment(environment: Environment): Builder {
            return super.setEnvironment(environment) as Builder
        }

        override fun buildInternal(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(this)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Adyen3DS2Configuration> = object : Parcelable.Creator<Adyen3DS2Configuration> {
            override fun createFromParcel(`in`: Parcel): Adyen3DS2Configuration {
                return Adyen3DS2Configuration(`in`)
            }

            override fun newArray(size: Int): Array<Adyen3DS2Configuration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
