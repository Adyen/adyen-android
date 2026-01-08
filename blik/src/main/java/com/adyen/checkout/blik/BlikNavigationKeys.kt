/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 17/12/2025.
 */

@file:Suppress("Filename", "ktlint:standard:filename", "MatchingDeclarationName")

package com.adyen.checkout.blik

import com.adyen.checkout.core.components.navigation.CheckoutNavigationKey
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes

/**
 * Represent the main form of the [PaymentMethodTypes.BLIK] payment method.
 */
data object BlikMainNavigationKey : CheckoutNavigationKey

/**
 * Represent the stored form of the [PaymentMethodTypes.BLIK] payment method.
 */
data object StoredBlikNavigationKey : CheckoutNavigationKey
