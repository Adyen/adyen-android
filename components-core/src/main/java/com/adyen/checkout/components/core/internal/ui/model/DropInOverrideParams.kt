/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/1/2024.
 */

package com.adyen.checkout.components.core.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.Amount

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class DropInOverrideParams(
    val amount: Amount?,
    val sessionParams: SessionParams?,
    val isSubmitButtonVisible: Boolean = true
)
