/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/10/2022.
 */

package com.adyen.checkout.example.ui

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import javax.inject.Inject
import javax.inject.Singleton

internal interface NightThemeRepository {

    var theme: NightTheme

    fun initialize()
}

@Singleton
internal class DefaultNightThemeRepository @Inject constructor(
    private val prefs: SharedPreferences,
) : NightThemeRepository {

    override var theme: NightTheme
        get() = getThemeFromPrefs()
        set(value) {
            prefs.edit { putString(PREF_KEY_NIGHT_THEME, value.preferenceValue) }
            AppCompatDelegate.setDefaultNightMode(getThemeFromPrefs().appCompatMode)
        }

    override fun initialize() {
        AppCompatDelegate.setDefaultNightMode(getThemeFromPrefs().appCompatMode)
    }

    private fun getThemeFromPrefs(): NightTheme {
        val preference = prefs.getString(PREF_KEY_NIGHT_THEME, NightTheme.SYSTEM.preferenceValue)
        return NightTheme.findByPreferenceValue(preference)
    }

    companion object {
        // Should be same as R.string.night_theme_key
        private const val PREF_KEY_NIGHT_THEME = "night_theme_key"
    }
}

internal enum class NightTheme(
    val preferenceValue: String,
    val appCompatMode: Int,
) {
    DAY("Light", AppCompatDelegate.MODE_NIGHT_NO),
    NIGHT("Dark", AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM("System", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    companion object {

        fun findByPreferenceValue(value: String?): NightTheme =
            values().find { it.preferenceValue == value } ?: SYSTEM
    }
}
