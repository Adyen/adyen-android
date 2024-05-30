/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 10/3/2023.
 */
package com.adyen.checkout.components.core.internal.util

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.data.model.StatusResponse

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object StatusResponseUtils {
    const val RESULT_PENDING = "pending"
    const val RESULT_AUTHORIZED = "authorised"
    const val RESULT_REFUSED = "refused"
    const val RESULT_ERROR = "error"
    const val RESULT_CANCELED = "canceled"

    fun isFinalResult(statusResponse: StatusResponse): Boolean {
        return RESULT_PENDING != statusResponse.resultCode
    }
}
