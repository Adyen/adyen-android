/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/7/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.data.model.Amount

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class PayButtonViewState(
    val amount: Amount?,
    val isLoading: Boolean,
)
