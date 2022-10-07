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
import com.adyen.checkout.issuerlist.IssuerListViewType
import java.util.Locale

class OnlineBankingPLConfiguration : IssuerListConfiguration {

    private constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        viewType: IssuerListViewType,
        hideIssuerLogos: Boolean,
    ) : super(shopperLocale, environment, clientKey, viewType, hideIssuerLogos)

    private constructor(parcel: Parcel) : super(parcel)

    class Builder : IssuerListBuilder<OnlineBankingPLConfiguration> {

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
        constructor(configuration: OnlineBankingPLConfiguration) : super(configuration) {
            viewType = configuration.viewType
            hideIssuerLogos = configuration.hideIssuerLogos
        }

        override fun buildInternal(): OnlineBankingPLConfiguration {
            return OnlineBankingPLConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
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
