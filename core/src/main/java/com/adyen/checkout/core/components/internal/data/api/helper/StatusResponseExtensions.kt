/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/7/2025.
 */
package com.adyen.checkout.core.components.internal.data.api.helper

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.data.model.StatusResponse

private const val RESULT_PENDING = "pending"

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun StatusResponse.isFinalResult() = RESULT_PENDING != resultCode
