/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 27/10/2025.
 */

package com.adyen.checkout.mbway

import com.adyen.checkout.core.components.navigation.CheckoutNavigationKey
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

/**
 * Represent the main form of the [PaymentMethodTypes.MB_WAY] payment method.
 */
data object MBWayMainNavigationKey : CheckoutNavigationKey

/**
 * Represent the country code picker opened from the [MBWayMainNavigationKey]'s content.
 */
data object MBWayCountryCodePickerNavigationKey : CheckoutNavigationKey
