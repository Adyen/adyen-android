/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.error.internal

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.internal.model.ErrorResponseBody

// TODO - Platform alignment: Review error name and structure after iOS alignment.
/**
 * Indicates that an HTTP API call has failed.
 *
 * @param code The HTTP status code of the failed request.
 * @param message A human-readable description of the error.
 * @param errorBody The parsed error response body from the server, if available.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class HttpError(
    val code: Int,
    message: String,
    val errorBody: ErrorResponseBody?,
) : NetworkError(message)
