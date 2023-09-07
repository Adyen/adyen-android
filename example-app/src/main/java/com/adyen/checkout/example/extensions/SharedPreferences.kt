/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 10/10/2019.
 */

package com.adyen.checkout.example.extensions

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes

internal fun SharedPreferences.getString(appContext: Context, @StringRes stringRes: Int, defaultValue: String): String {
    val key = appContext.getString(stringRes)
    return getString(key, null) ?: defaultValue
}

internal fun SharedPreferences.getString(
    appContext: Context,
    @StringRes stringRes: Int,
    @StringRes defaultStringRes: Int
): String {
    val key = appContext.getString(stringRes)
    return getString(key, null) ?: appContext.getString(defaultStringRes)
}

internal fun SharedPreferences.getBoolean(
    appContext: Context,
    @StringRes stringRes: Int,
    @StringRes defaultStringRes: Int
): Boolean {
    val key = appContext.getString(stringRes)
    return getBoolean(key, appContext.getString(defaultStringRes).toBoolean())
}
