/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/8/2020.
 */
package com.adyen.checkout.adyen3ds2

import android.content.Context
import android.content.IntentFilter
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.threeds2.internal.ui.activity.ChallengeActivity
import java.util.Locale

class Adyen3DS2Configuration : Configuration {

    val threeDSRequestorAppURL: String?

    private constructor(builder: Builder) : super(
        builder.builderShopperLocale,
        builder.builderEnvironment,
        builder.builderClientKey
    ) {
        threeDSRequestorAppURL = builder.threeDSRequestorAppURL
    }

    private constructor(inputParcel: Parcel) : super(inputParcel) {
        threeDSRequestorAppURL = inputParcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(threeDSRequestorAppURL)
    }

    /**
     * Builder to create a [Adyen3DS2Configuration].
     */
    class Builder : BaseConfigurationBuilder<Adyen3DS2Configuration> {

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

        internal var threeDSRequestorAppURL: String? = null
            private set

        /**
         * Constructor that copies an existing configuration.
         *
         * @param configuration A configuration to initialize the builder.
         */
        constructor(configuration: Adyen3DS2Configuration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        /**
         * Sets the 3DS Requestor App URL. This is used to call your app after an out-of-band (OOB)
         * authentication occurs.
         *
         * Make sure to also override [ChallengeActivity]'s [IntentFilter] with your own URL like
         * [this](https://docs.adyen.com/online-payments/classic-integrations/api-integration-ecommerce/3d-secure/native-3ds2/android-sdk-integration#handling-android-app-links)
         * when using this method.
         */
        @Suppress("MaxLineLength")
        fun setThreeDSRequestorAppURL(threeDSRequestorAppURL: String): Builder {
            this.threeDSRequestorAppURL = threeDSRequestorAppURL
            return this
        }

        override fun buildInternal(): Adyen3DS2Configuration {
            return Adyen3DS2Configuration(this)
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Adyen3DS2Configuration> =
            object : Parcelable.Creator<Adyen3DS2Configuration> {
                override fun createFromParcel(`in`: Parcel): Adyen3DS2Configuration {
                    return Adyen3DS2Configuration(`in`)
                }

                override fun newArray(size: Int): Array<Adyen3DS2Configuration?> {
                    return arrayOfNulls(size)
                }
            }
    }
}
