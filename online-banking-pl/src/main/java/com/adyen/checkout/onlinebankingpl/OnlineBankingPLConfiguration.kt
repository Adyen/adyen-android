/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 10/8/2022.
 */

package com.adyen.checkout.onlinebankingpl

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import java.util.Locale

class OnlineBankingPLConfiguration : IssuerListConfiguration {

    private constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
    ) : super(shopperLocale, environment, clientKey)

    private constructor(parcel: Parcel) : super(parcel)

    class Builder : IssuerListBuilder<OnlineBankingPLConfiguration> {
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
        constructor(configuration: OnlineBankingPLConfiguration) : super(configuration)

        override fun setShopperLocale(builderShopperLocale: Locale): Builder {
            return super.setShopperLocale(builderShopperLocale) as Builder
        }

        override fun setEnvironment(builderEnvironment: Environment): Builder {
            return super.setEnvironment(builderEnvironment) as Builder
        }

        override fun buildInternal(): OnlineBankingPLConfiguration {
            return OnlineBankingPLConfiguration(
                shopperLocale = builderShopperLocale,
                environment = builderEnvironment,
                clientKey = builderClientKey,
            )
        }
    }

    companion object CREATOR : Parcelable.Creator<OnlineBankingPLConfiguration> {
        override fun createFromParcel(parcel: Parcel): OnlineBankingPLConfiguration {
            return OnlineBankingPLConfiguration(parcel)
        }

        override fun newArray(size: Int): Array<OnlineBankingPLConfiguration?> {
            return arrayOfNulls(size)
        }
    }
}
