/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/9/2022.
 */

package com.adyen.checkout.onlinebankingsk

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.onlinebankingcore.OnlineBankingConfiguration
import java.util.Locale

class OnlineBankingSKConfiguration : OnlineBankingConfiguration {

    private constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
    ) : super(shopperLocale, environment, clientKey)

    private constructor(parcel: Parcel) : super(parcel)

    class Builder : OnlineBankingBuilder<OnlineBankingSKConfiguration> {

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
        constructor(configuration: OnlineBankingSKConfiguration) : super(configuration)

        override fun setShopperLocale(shopperLocale: Locale): Builder {
            return super.setShopperLocale(shopperLocale) as Builder
        }

        override fun setEnvironment(environment: Environment): Builder {
            return super.setEnvironment(environment) as Builder
        }

        override fun buildInternal(): OnlineBankingSKConfiguration {
            return OnlineBankingSKConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
            )
        }
    }

    companion object CREATOR : Parcelable.Creator<OnlineBankingConfiguration> {
        override fun createFromParcel(parcel: Parcel): OnlineBankingConfiguration {
            return OnlineBankingSKConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<OnlineBankingConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}
