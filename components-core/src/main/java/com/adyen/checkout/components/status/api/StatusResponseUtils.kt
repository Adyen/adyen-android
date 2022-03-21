/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 8/9/2020.
 */
package com.adyen.checkout.components.status.api

import com.adyen.checkout.components.status.model.StatusResponse

object StatusResponseUtils {
    const val RESULT_PENDING = "pending"
    const val RESULT_AUTHORIZED = "authorised"
    const val RESULT_REFUSED = "refused"
    const val RESULT_ERROR = "error"
    const val RESULT_CANCELED = "canceled"

    @JvmStatic
    fun isFinalResult(statusResponse: StatusResponse): Boolean {
        return RESULT_PENDING != statusResponse.resultCode
    }
}
