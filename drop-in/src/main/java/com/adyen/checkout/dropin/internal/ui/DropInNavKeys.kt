/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/11/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
internal data object EmptyNavKey : NavKey

@Serializable
internal data object PreselectedPaymentMethodNavKey : NavKey

@Serializable
internal data object PaymentMethodListNavKey : NavKey

@Serializable
internal data object ManageFavoritesNavKey : NavKey

@Serializable
internal data object PaymentMethodNavKey : NavKey
