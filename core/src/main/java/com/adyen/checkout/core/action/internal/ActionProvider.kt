/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.analytics.internal.AnalyticsManager
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import kotlinx.coroutines.CoroutineScope

internal class ActionProvider(
    val analyticsManager: AnalyticsManager,
    val checkoutConfiguration: CheckoutConfiguration,
    val savedStateHandle: SavedStateHandle,
    val commonComponentParams: CommonComponentParams
) {

    fun get(
        action: Action,
        coroutineScope: CoroutineScope,
    ) = ActionComponentProvider.get(
        action = action,
        coroutineScope = coroutineScope,
        analyticsManager = analyticsManager,
        checkoutConfiguration = checkoutConfiguration,
        savedStateHandle = savedStateHandle,
        commonComponentParams = commonComponentParams,
    )
}
