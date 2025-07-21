/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/7/2025.
 */

package com.adyen.checkout.example.ui.v6

import android.os.Bundle
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.components.AdyenPaymentFlow
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.example.ui.theme.UIThemeRepository
import com.adyen.checkout.ui.theme.AdyenCheckoutTheme
import com.adyen.checkout.ui.theme.AdyenColors
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class V6SessionsActivity : AppCompatActivity() {

    private val viewModel: V6SessionsViewModel by viewModels()

    @Inject
    internal lateinit var uiThemeRepository: UIThemeRepository

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val theme = AdyenCheckoutTheme(
            colors = if (uiThemeRepository.isDarkTheme(this)) {
                AdyenColors.dark()
            } else {
                AdyenColors.light()
            },
        )

        setContent {
            val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
            ExampleTheme(uiThemeRepository.isDarkTheme()) {
                Scaffold(
                    containerColor = Color(theme.colors.background.value),
                    topBar = {
                        TopAppBar(
                            title = { Text("v6 components") },
                            navigationIcon = {
                                IconButton(onClick = { backPressedDispatcher?.onBackPressed() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                        )
                    },
                ) { contentPadding ->
                    viewModel.adyenCheckout?.let {
                        AdyenPaymentFlow(
                            txVariant = "mbway",
                            adyenCheckout = it,
                            theme = theme,
                            modifier = Modifier
                                .padding(contentPadding)
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
