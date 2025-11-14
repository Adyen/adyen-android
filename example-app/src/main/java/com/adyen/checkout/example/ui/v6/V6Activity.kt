/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import com.adyen.checkout.redirect.old.RedirectComponent
import com.adyen.checkout.ui.theme.CheckoutColors
import com.adyen.checkout.ui.theme.CheckoutTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class V6Activity : AppCompatActivity() {

    private val viewModel: V6ViewModel by viewModels()

    @Inject
    internal lateinit var uiThemeRepository: UIThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Insert return url in extras, so we can access it in the ViewModel through SavedStateHandle
        val returnUrl = RedirectComponent.getReturnUrl(applicationContext) + "/v6"
        intent = (intent ?: Intent()).putExtra(RETURN_URL_EXTRA, returnUrl)

        val theme = CheckoutTheme(
            colors = if (uiThemeRepository.isDarkTheme(this)) {
                CheckoutColors.dark()
            } else {
                CheckoutColors.light()
            },
        )

        setContent {
            ExampleTheme(uiThemeRepository.isDarkTheme()) {
                V6Screen(
                    theme = theme,
                    uiState = viewModel.uiState,
                    checkoutController = viewModel.checkoutController,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val data = intent.data
        if (data != null && data.toString().startsWith(RedirectComponent.REDIRECT_RESULT_SCHEME)) {
            viewModel.handleIntent(intent)
        }
    }

    companion object {
        internal const val RETURN_URL_EXTRA = "RETURN_URL_EXTRA"
    }
}
