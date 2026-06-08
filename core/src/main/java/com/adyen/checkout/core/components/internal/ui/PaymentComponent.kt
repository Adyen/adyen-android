/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 8/4/2025.
 */

package com.adyen.checkout.core.components.internal.ui

import androidx.annotation.RestrictTo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.adyen.checkout.core.components.internal.PaymentComponentEvent

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PaymentComponent : EventComponent<PaymentComponentEvent> {

    @Composable
    fun Content(modifier: Modifier)

    fun submit()

    fun requiresUserInteraction(): Boolean

    fun setLoading(isLoading: Boolean)

    fun onCleared()
}
