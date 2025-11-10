/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.core.components.navigation

fun interface CheckoutNavigationProvider {
    fun provide(key: CheckoutNavigationKey): CheckoutNavigationProperties?
}
