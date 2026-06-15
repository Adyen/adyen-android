/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by robertsc on 15/6/2026.
 */

package com.adyen.checkout.example.data.api.model

import androidx.annotation.Keep
import com.adyen.checkout.core.components.data.model.Amount

@Keep
data class SessionPatchRequest(
    val sessionData: String,
    val amount: Amount,
    val payable: Boolean = true,
)
