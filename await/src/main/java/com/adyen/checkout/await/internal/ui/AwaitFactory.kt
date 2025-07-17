/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/7/2025.
 */

package com.adyen.checkout.await.internal.ui

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.internal.ActionFactory
import com.adyen.checkout.core.components.CheckoutConfiguration
import kotlinx.coroutines.CoroutineScope

internal class AwaitFactory : ActionFactory<AwaitDelegate> {

    override fun create(
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle
    ): AwaitDelegate = AwaitDelegate()
}
