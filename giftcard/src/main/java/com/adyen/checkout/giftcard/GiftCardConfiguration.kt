/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */
package com.adyen.checkout.giftcard

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

class GiftCardConfiguration : Configuration {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<GiftCardConfiguration?> = object : Parcelable.Creator<GiftCardConfiguration?> {
            override fun createFromParcel(source: Parcel?): GiftCardConfiguration? {
                if (source == null) return null
                return GiftCardConfiguration(source)
            }

            override fun newArray(size: Int): Array<GiftCardConfiguration?> {
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
     * Builder to create a [GiftCardConfiguration].
     */
    class Builder : BaseConfigurationBuilder<GiftCardConfiguration> {
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
        constructor(configuration: GiftCardConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): GiftCardConfiguration {
            return GiftCardConfiguration(this)
        }
    }
}
