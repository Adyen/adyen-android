/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/11/2022.
 */

package com.adyen.checkout.ui.core.old.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.util.mergeStateFlows
import com.adyen.checkout.ui.core.internal.ui.ComponentViewType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.drop

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun mergeViewFlows(
    scope: CoroutineScope,
    paymentMethodViewFlow: Flow<ComponentViewType?>,
    genericActionViewFlow: Flow<ComponentViewType?>,
    initialValue: ComponentViewType? = null,
    // Lazily is the default, because WhileSubscribed re-emits initial values when f.e. rotating the phone.
    started: SharingStarted = SharingStarted.Lazily,
): StateFlow<ComponentViewType?> = mergeStateFlows(
    scope = scope,
    initialValue = initialValue,
    started = started,
    // genericActionViewFlow's initial value is null. We don't need this value as it breaks the desired behaviour.
    flows = arrayOf(paymentMethodViewFlow, genericActionViewFlow.drop(1)),
)
