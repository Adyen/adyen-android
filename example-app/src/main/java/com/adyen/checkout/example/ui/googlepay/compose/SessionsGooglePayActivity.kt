/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

package com.adyen.checkout.example.ui.googlepay.compose

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import com.adyen.checkout.redirect.RedirectComponent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SessionsGooglePayActivity : AppCompatActivity() {

    @Inject
    internal lateinit var uiThemeRepository: UIThemeRepository

    private val sessionsGooglePayViewModel: SessionsGooglePayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Helps to resize the view port when the keyboard is displayed.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/sessions/googlepay"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        setContent {
            val googlePayState by sessionsGooglePayViewModel.googlePayState.collectAsState()
            val eventsState by sessionsGooglePayViewModel.stateEvents.collectAsState()
            val isDarkTheme = uiThemeRepository.isDarkTheme()
            ExampleTheme(isDarkTheme) {
                SessionsGooglePayScreen(
                    useDarkTheme = isDarkTheme,
                    onBackPressed = { onBackPressedDispatcher.onBackPressed() },
                    googlePayState = googlePayState,
                    eventsState = eventsState,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            sessionsGooglePayViewModel.onNewIntent(intent)
        }
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
