/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/4/2021.
 */
package com.adyen.checkout.qrcode

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

class QRCodeConfiguration : Configuration {

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<QRCodeConfiguration?> = object : Parcelable.Creator<QRCodeConfiguration?> {
            override fun createFromParcel(source: Parcel?): QRCodeConfiguration? {
                if (source == null) return null
                return QRCodeConfiguration(source)
            }

            override fun newArray(size: Int): Array<QRCodeConfiguration?> {
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
     * Builder to create a [QRCodeConfiguration].
     */
    class Builder : BaseConfigurationBuilder<QRCodeConfiguration> {
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
        constructor(configuration: QRCodeConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): QRCodeConfiguration {
            return QRCodeConfiguration(this)
        }
    }
}
