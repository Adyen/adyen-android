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
    val protocolVersion: String?

    private constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        protocolVersion: String?
    ) : super(shopperLocale, environment, clientKey) {
        this.protocolVersion = protocolVersion
    }

    private constructor(`in`: Parcel) : super(`in`) {
        protocolVersion = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(protocolVersion)
    }

    /**
     * Builder to create a [Adyen3DS2Configuration].
     */
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration?> {
        private var mBuilderProtocolVersion: String? = PROTOCOL_2_1_0

        /**
         * Constructor for Builder with default values.
         *
         * @param context   A context
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(context: Context, clientKey: String) : super(context, clientKey) {}

        /**
         * Builder with required parameters.
         *
         * @param shopperLocale The Locale of the shopper.
         * @param environment   The [Environment] to be used for network calls to Adyen.
         * @param clientKey Your Client Key used for network calls from the SDK to Adyen.
         */
        constructor(shopperLocale: Locale, environment: Environment, clientKey: String) : super(shopperLocale, environment, clientKey) {}

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        fun setProtocolVersion(builderProtocolVersion: String?): Builder {
            mBuilderProtocolVersion = builderProtocolVersion
            return this
        }

        override fun build(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(mBuilderShopperLocale, mBuilderEnvironment, mBuilderClientKey, mBuilderProtocolVersion)
        }
    }

    companion object {
        const val PROTOCOL_2_1_0 = "2.1.0"
        const val PROTOCOL_2_2_0 = "2.2.0"

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