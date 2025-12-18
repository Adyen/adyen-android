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

internal data class SdkData(
    val schemaVersion: Int,
    val analytics: Analytics? = null,
    val authentication: Authentication? = null,
    val createdAt: Long? = null,
    val supportNativeRedirect: Boolean? = null,
) {

    @Throws(JSONException::class)
    fun serialize() = JSONObject().apply {
        putOpt(SCHEMA_VERSION, schemaVersion)
        putOpt(ANALYTICS, analytics?.serialize())
        putOpt(AUTHENTICATION, authentication?.serialize())
        putOpt(CREATED_AT, createdAt)
        putOpt(SUPPORT_NATIVE_REDIRECT, supportNativeRedirect)
    }

    companion object {
        private const val SCHEMA_VERSION = "schemaVersion"
        private const val ANALYTICS = "analytics"
        private const val AUTHENTICATION = "authentication"
        private const val CREATED_AT = "createdAt"
        private const val SUPPORT_NATIVE_REDIRECT = "supportNativeRedirect"
    }
}
