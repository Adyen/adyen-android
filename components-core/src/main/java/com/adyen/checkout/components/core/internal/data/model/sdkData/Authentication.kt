/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 16/10/2025.
 */

package com.adyen.checkout.components.core.internal.data.model.sdkData

import org.json.JSONException
import org.json.JSONObject

internal data class Authentication(
    val threeDS2SdkVersion: String? = null
) {

    @Throws(JSONException::class)
    fun serialize() = JSONObject().apply {
        putOpt(THREEDS2_SDK_VERSION, threeDS2SdkVersion)
    }

    companion object {
        private const val THREEDS2_SDK_VERSION = "threeDS2SdkVersion"
    }
}
