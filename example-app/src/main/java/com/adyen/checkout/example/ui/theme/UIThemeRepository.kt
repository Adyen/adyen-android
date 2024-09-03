/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/10/2022.
 */

package com.adyen.checkout.example.ui.theme

import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import com.adyen.checkout.example.data.storage.SharedPreferencesEntry
import com.adyen.checkout.example.data.storage.SharedPreferencesManager
import javax.inject.Inject
import javax.inject.Singleton

internal interface UIThemeRepository {

    var theme: UITheme

    fun initialize()

    @Composable
    fun isDarkTheme(): Boolean

    fun isDarkTheme(context: Context): Boolean
}

@Singleton
internal class DefaultUIThemeRepository @Inject constructor(
    private val sharedPreferencesManager: SharedPreferencesManager,
) : UIThemeRepository {

    override var theme: UITheme
        get() = getThemeFromPrefs()
        set(value) {
            sharedPreferencesManager.putEnum(SharedPreferencesEntry.UI_THEME, value)
            AppCompatDelegate.setDefaultNightMode(getThemeFromPrefs().appCompatMode)
        }

    override fun initialize() {
        AppCompatDelegate.setDefaultNightMode(getThemeFromPrefs().appCompatMode)
    }

    private fun getThemeFromPrefs(): UITheme {
        return sharedPreferencesManager.getEnum(SharedPreferencesEntry.UI_THEME)
    }

    @Composable
    override fun isDarkTheme(): Boolean {
        return when (theme) {
            UITheme.LIGHT -> false
            UITheme.DARK -> true
            UITheme.SYSTEM -> isSystemInDarkTheme()
        }
    }

    override fun isDarkTheme(context: Context): Boolean {
        return when (theme) {
            UITheme.LIGHT -> false
            UITheme.DARK -> true
            UITheme.SYSTEM -> {
                when (context.resources.configuration.uiMode.and(Configuration.UI_MODE_NIGHT_MASK)) {
                    Configuration.UI_MODE_NIGHT_YES -> true
                    Configuration.UI_MODE_NIGHT_NO -> false
                    Configuration.UI_MODE_NIGHT_UNDEFINED -> false
                    else -> false
                }
            }
        }
    }
}

enum class UITheme(
    val appCompatMode: Int,
) {
    LIGHT(AppCompatDelegate.MODE_NIGHT_NO),
    DARK(AppCompatDelegate.MODE_NIGHT_YES),
    SYSTEM(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
}
