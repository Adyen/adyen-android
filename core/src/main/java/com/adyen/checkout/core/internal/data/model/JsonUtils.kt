/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 30/4/2025.
 */

package com.adyen.checkout.core.internal.data.model

import androidx.annotation.RestrictTo
import org.json.JSONObject

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getStringOrNull(key: String): String? {
    return if (!isNull(key)) getString(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getBooleanOrNull(key: String): Boolean? {
    return if (!isNull(key)) getBoolean(key) else null
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun JSONObject.getLongOrNull(key: String): Long? {
    return if (!isNull(key)) getLong(key) else null
}
