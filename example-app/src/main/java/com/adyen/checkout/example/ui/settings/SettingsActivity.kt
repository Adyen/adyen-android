/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/8/2024.
 */

package com.adyen.checkout.example.ui.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.adyen.checkout.example.ui.settings.composable.SettingsScreen
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    @Inject
    internal lateinit var uiThemeRepository: UIThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Helps to resize the view port when the keyboard is displayed.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val isDarkTheme = uiThemeRepository.isDarkTheme()
            ExampleTheme(isDarkTheme) {
                SettingsScreen(onBackPressed = { onBackPressedDispatcher.onBackPressed() })
            }
        }
    }
}
