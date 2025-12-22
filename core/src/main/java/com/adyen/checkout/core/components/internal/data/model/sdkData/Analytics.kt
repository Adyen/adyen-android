/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.core.components.internal.data.model.sdkData

import org.json.JSONException
import org.json.JSONObject

internal data class Analytics(
    val checkoutAttemptId: String? = null
) {

    @Throws(JSONException::class)
    fun serialize() = JSONObject().apply {
        putOpt(CHECKOUT_ATTEMPT_ID, checkoutAttemptId)
    }

    companion object {
        private const val CHECKOUT_ATTEMPT_ID = "checkoutAttemptId"
    }
}
