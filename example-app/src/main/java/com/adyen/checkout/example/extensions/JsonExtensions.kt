/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/5/2023.
 */

package com.adyen.checkout.example.extensions

import org.json.JSONException
import org.json.JSONObject

private const val INDENTATION_SPACES = 4
private const val PARSING_ERROR = "PARSING_ERROR"

internal fun JSONObject.toStringPretty(): String {
    @Suppress("SwallowedException")
    return try {
        toString(INDENTATION_SPACES)
    } catch (e: JSONException) {
        PARSING_ERROR
    }
}
