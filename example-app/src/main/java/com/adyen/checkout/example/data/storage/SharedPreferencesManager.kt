/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 29/8/2024.
 */

package com.adyen.checkout.example.data.storage

import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesManager(
    val sharedPreferences: SharedPreferences
) {

    fun getString(entry: SharedPreferencesEntry): String {
        if (entry.defaultValue !is String) {
            error("Preference is not a non-nullable String")
        }
        return sharedPreferences.getString(entry.key, null) ?: entry.defaultValue
    }

    fun getStringNullable(entry: SharedPreferencesEntry): String? {
        if (entry.defaultValue != null) {
            error("Preference is not a nullable String")
        }
        return sharedPreferences.getString(entry.key, null)
    }

    fun getBoolean(entry: SharedPreferencesEntry): Boolean {
        if (entry.defaultValue !is Boolean) {
            error("Preference is not a Boolean")
        }
        return sharedPreferences.getBoolean(entry.key, entry.defaultValue)
    }

    fun getLong(entry: SharedPreferencesEntry): Long {
        if (entry.defaultValue !is Long) {
            error("Preference is not a Long")
        }
        return sharedPreferences.getString(entry.key, null)?.toLong() ?: entry.defaultValue
    }

    inline fun <reified T : Enum<T>> getEnum(entry: SharedPreferencesEntry): T {
        if (entry.defaultValue !is T) {
            error("Preference does not match the required type")
        }
        val stringValue = sharedPreferences.getString(entry.key, null) ?: return entry.defaultValue
        return enumValueOf(stringValue)
    }

    fun putString(entry: SharedPreferencesEntry, value: String?) {
        return sharedPreferences.edit {
            putString(entry.key, value)
        }
    }

    fun putBoolean(entry: SharedPreferencesEntry, value: Boolean) {
        return sharedPreferences.edit {
            putBoolean(entry.key, value)
        }
    }

    fun putLong(entry: SharedPreferencesEntry, value: Long?) {
        return sharedPreferences.edit {
            putString(entry.key, value?.toString())
        }
    }

    fun putEnum(entry: SharedPreferencesEntry, value: Enum<*>) {
        return sharedPreferences.edit {
            putString(entry.key, value.toString())
        }
    }
}
