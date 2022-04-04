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
        builder.builderShopperLocale,
        builder.builderEnvironment,
        builder.builderClientKey
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

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
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
