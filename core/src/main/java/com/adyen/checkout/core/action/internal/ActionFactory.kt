/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/7/2025.
 */

package com.adyen.checkout.core.action.internal

import androidx.annotation.RestrictTo
import androidx.lifecycle.SavedStateHandle
import com.adyen.checkout.core.action.data.Action
import com.adyen.checkout.core.components.CheckoutConfiguration
import com.adyen.checkout.core.components.internal.ui.model.CommonComponentParams
import kotlinx.coroutines.CoroutineScope

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface ActionFactory<T : ActionComponent> {

    fun create(
        action: Action,
        coroutineScope: CoroutineScope,
        checkoutConfiguration: CheckoutConfiguration,
        savedStateHandle: SavedStateHandle,
        commonComponentParams: CommonComponentParams,
    ): T
}
