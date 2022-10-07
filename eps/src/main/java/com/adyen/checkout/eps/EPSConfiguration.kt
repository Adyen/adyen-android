/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 12/6/2019.
 */
package com.adyen.checkout.eps

import android.content.Context
import android.os.Parcel
import android.os.Parcelable
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.issuerlist.IssuerListConfiguration
import com.adyen.checkout.issuerlist.IssuerListViewType
import java.util.Locale

class EPSConfiguration : IssuerListConfiguration {

    private constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        viewType: IssuerListViewType,
        hideIssuerLogos: Boolean,
    ) : super(shopperLocale, environment, clientKey, viewType, hideIssuerLogos)

    private constructor(parcel: Parcel) : super(parcel)

    /**
     * Builder to create a [EPSConfiguration].
     */
    class Builder : IssuerListBuilder<EPSConfiguration> {

        override var hideIssuerLogos: Boolean = true

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
        constructor(configuration: EPSConfiguration) : super(configuration) {
            viewType = configuration.viewType
            hideIssuerLogos = configuration.hideIssuerLogos
        }

        public override fun buildInternal(): EPSConfiguration {
            return EPSConfiguration(
                shopperLocale = shopperLocale,
                environment = environment,
                clientKey = clientKey,
                viewType = viewType,
                hideIssuerLogos = hideIssuerLogos,
            )
        }
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<EPSConfiguration> = object : Parcelable.Creator<EPSConfiguration> {
            override fun createFromParcel(parcel: Parcel): EPSConfiguration {
                return EPSConfiguration(parcel)
            }

            override fun newArray(size: Int): Array<EPSConfiguration?> {
                return arrayOfNulls(size)
            }
        }
    }
}
