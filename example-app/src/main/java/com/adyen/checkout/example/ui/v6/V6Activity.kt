/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/6/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
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
                )
            }
        }
    }
}
