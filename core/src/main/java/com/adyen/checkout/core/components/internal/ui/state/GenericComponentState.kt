/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class GenericComponentState(
    val isLoading: Boolean,
) : ComponentState
