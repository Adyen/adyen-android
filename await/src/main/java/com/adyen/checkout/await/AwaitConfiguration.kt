/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 19/8/2020.
 */
package com.adyen.checkout.await

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

class AwaitConfiguration : Configuration {

    constructor(builder: Builder) : super(
        builder.shopperLocale,
        builder.environment,
        builder.clientKey
    )

    constructor(parcel: Parcel) : super(parcel)

    /**
     * Builder to create a [AwaitConfiguration].
     */
    class Builder : BaseConfigurationBuilder<AwaitConfiguration> {

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
        constructor(configuration: AwaitConfiguration) : super(configuration)

        override fun setShopperLocale(shopperLocale: Locale): Builder {
            return super.setShopperLocale(shopperLocale) as Builder
        }

        override fun setEnvironment(environment: Environment): Builder {
            return super.setEnvironment(environment) as Builder
        }

        override fun buildInternal(): AwaitConfiguration {
            return AwaitConfiguration(this)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AwaitConfiguration> = object : Parcelable.Creator<AwaitConfiguration> {
            override fun createFromParcel(`in`: Parcel): AwaitConfiguration {
                return AwaitConfiguration(`in`)
            }

            override fun newArray(size: Int): Array<AwaitConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
