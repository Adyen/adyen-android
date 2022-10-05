/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 25/4/2019.
 */
package com.adyen.checkout.issuerlist

import android.content.Context
import android.os.Parcel
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import com.adyen.checkout.core.util.ParcelUtils
import java.util.Locale

abstract class IssuerListConfiguration : Configuration {

    val viewType: IssuerListViewType
    val hideIssuerLogos: Boolean

    protected constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String,
        viewType: IssuerListViewType,
        hideIssuerLogos: Boolean,
    ) : super(shopperLocale, environment, clientKey) {
        this.viewType = viewType
        this.hideIssuerLogos = hideIssuerLogos
    }

    protected constructor(parcel: Parcel) : super(parcel) {
        viewType = IssuerListViewType.valueOf(parcel.readString()!!)
        hideIssuerLogos = ParcelUtils.readBoolean(parcel)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        super.writeToParcel(parcel, flags)
        parcel.writeString(viewType.name)
        ParcelUtils.writeBoolean(parcel, hideIssuerLogos)
    }

    abstract class IssuerListBuilder<IssuerListConfigurationT : IssuerListConfiguration> :
        BaseConfigurationBuilder<IssuerListConfigurationT> {

        protected open var viewType: IssuerListViewType = IssuerListViewType.RECYCLER_VIEW
        protected open var hideIssuerLogos: Boolean = false

        protected constructor(context: Context, environment: Environment, clientKey: String) : super(
            context,
            environment,
            clientKey
        )

        protected constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        protected constructor(configuration: IssuerListConfigurationT) : super(configuration)

        /**
         * Sets the type of the view to be show with the component.
         */
        fun setViewType(viewType: IssuerListViewType): IssuerListBuilder<IssuerListConfigurationT> {
            this.viewType = viewType
            return this
        }

        /**
         * Sets whether the logos should be shows next to the issuers name.
         */
        fun setHideIssuerLogos(hideIssuerLogos: Boolean): IssuerListBuilder<IssuerListConfigurationT> {
            this.hideIssuerLogos = hideIssuerLogos
            return this
        }
    }
}
