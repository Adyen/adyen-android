/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 21/2/2025.
 */

package com.adyen.checkout.core.internal.data.api

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class AdyenApiResponse(
    val path: String,
    val statusCode: Int,
    val headers: Map<String, String>,
    val body: String,
)
