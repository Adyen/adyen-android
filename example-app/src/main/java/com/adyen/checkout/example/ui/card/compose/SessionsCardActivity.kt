/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/11/2023.
 */

package com.adyen.checkout.example.ui.card.compose

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.core.view.WindowCompat
import com.adyen.checkout.example.ui.theme.NightTheme
import com.adyen.checkout.example.ui.theme.NightThemeRepository
import com.adyen.checkout.redirect.RedirectComponent
import com.adyen.checkout.example.ui.theme.ExampleTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SessionsCardActivity : AppCompatActivity() {

    @Inject
    internal lateinit var nightThemeRepository: NightThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Helps to resize the view port when the keyboard is displayed.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/sessions/card"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        setContent {
            val useDarkTheme = when (nightThemeRepository.theme) {
                NightTheme.DAY -> false
                NightTheme.NIGHT -> true
                NightTheme.SYSTEM -> isSystemInDarkTheme()
            }
            ExampleTheme(useDarkTheme) {
                SessionsCardScreen(onBackPressed = { onBackPressedDispatcher.onBackPressed() })
            }
        }
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
