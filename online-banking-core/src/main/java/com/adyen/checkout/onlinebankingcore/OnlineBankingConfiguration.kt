/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 23/8/2022.
 */

package com.adyen.checkout.onlinebankingcore

import android.content.Context
import android.os.Parcel
import com.adyen.checkout.components.base.BaseConfigurationBuilder
import com.adyen.checkout.components.base.Configuration
import com.adyen.checkout.core.api.Environment
import java.util.Locale

abstract class OnlineBankingConfiguration : Configuration {

    protected constructor(
        shopperLocale: Locale,
        environment: Environment,
        clientKey: String
    ) : super(shopperLocale, environment, clientKey)

    protected constructor(parcel: Parcel) : super(parcel)

    abstract class OnlineBankingBuilder<OnlineBankingConfigurationT : OnlineBankingConfiguration> :
        BaseConfigurationBuilder<OnlineBankingConfigurationT> {

        protected constructor(context: Context, clientKey: String) : super(context, clientKey)

        protected constructor(
            shopperLocale: Locale,
            environment: Environment,
            clientKey: String
        ) : super(shopperLocale, environment, clientKey)

        protected constructor(configuration: OnlineBankingConfigurationT) : super(configuration)
    }
}
