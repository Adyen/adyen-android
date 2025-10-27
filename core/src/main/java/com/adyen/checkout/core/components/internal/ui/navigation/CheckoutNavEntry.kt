/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/10/2025.
 */

package com.adyen.checkout.core.components.internal.ui.navigation

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import com.adyen.checkout.core.components.navigation.CheckoutDisplayStrategy
import com.adyen.checkout.core.components.navigation.CheckoutNavigationKey

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CheckoutNavEntry(
    val key: NavKey,
    val publicKey: CheckoutNavigationKey,
    val displayStrategy: CheckoutDisplayStrategy = CheckoutDisplayStrategy.INLINE,
    val content: @Composable (NavBackStack<NavKey>) -> Unit,
)
