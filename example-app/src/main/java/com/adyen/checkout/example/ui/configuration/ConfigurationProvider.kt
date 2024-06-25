/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.example.ui.configuration

import com.adyen.checkout.components.core.CheckoutConfiguration

internal interface ConfigurationProvider {

    val checkoutConfig: CheckoutConfiguration
}
