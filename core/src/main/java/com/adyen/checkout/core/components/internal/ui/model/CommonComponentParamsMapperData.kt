/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 6/5/2025.
 */

package com.adyen.checkout.core.components.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.sessions.internal.model.SessionParams

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CommonComponentParamsMapperData(
    val commonComponentParams: CommonComponentParams,
    val sessionParams: SessionParams?,
)
