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
import java.util.Locale

abstract class IssuerListConfiguration : Configuration {
    protected constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String
    ) : super(shopperLocale, environment, clientKey)

    protected constructor(parcel: Parcel) : super(parcel)

    abstract class IssuerListBuilder<IssuerListConfigurationT : IssuerListConfiguration> :
        BaseConfigurationBuilder<IssuerListConfigurationT> {

        protected constructor(context: Context, clientKey: String) : super(context, clientKey)

        protected constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        protected constructor(configuration: IssuerListConfigurationT) : super(configuration)
    }
}
