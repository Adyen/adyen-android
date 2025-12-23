/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 4/11/2025.
 */

package com.adyen.checkout.card

import com.adyen.checkout.core.components.navigation.CheckoutNavigationKey
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

/**
 * Represent the main form of the [PaymentMethodTypes.SCHEME] payment method.
 */
data object CardMainNavigationKey : CheckoutNavigationKey

/**
 * Represent the stored form of the [PaymentMethodTypes.SCHEME] payment method.
 */
data object StoredCardNavigationKey : CheckoutNavigationKey

/**
 * Represent the address form opened from the [CardMainNavigationKey]'s content.
 */
@Suppress("unused")
data object CardAddressNavigationKey : CheckoutNavigationKey
