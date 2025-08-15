/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2025.
 */

package com.adyen.checkout.example.ui.v6

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.adyen.checkout.core.components.AdyenPaymentFlow
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.ui.theme.CheckoutTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun V6Screen(
    theme: CheckoutTheme,
    uiState: V6UiState,
) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    Scaffold(
        containerColor = Color(theme.colors.background.value),
        topBar = {
            TopAppBar(
                title = { Text("v6 components") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(theme.colors.background.value),
                ),
                navigationIcon = {
                    IconButton(onClick = { backPressedDispatcher?.onBackPressed() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { contentPadding ->
        when (uiState) {
            is V6UiState.Component -> Component(theme, uiState, Modifier.padding(contentPadding))
            is V6UiState.Final -> Final(uiState, Modifier.padding(contentPadding))
            is V6UiState.Error -> Error(uiState, Modifier.padding(contentPadding))
            is V6UiState.Loading -> Loading(Modifier.padding(contentPadding))
        }
    }
}

@Composable
private fun Component(
    theme: CheckoutTheme,
    uiState: V6UiState.Component,
    modifier: Modifier,
) {
    AdyenPaymentFlow(
        txVariant = "mbway",
        checkoutContext = uiState.checkoutContext,
        theme = theme,
        modifier = modifier.padding(16.dp),
    )
}

@Composable
private fun Final(
    uiState: V6UiState.Final,
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        ResultContent(uiState.resultState)
    }
}

@Composable
private fun Error(
    uiState: V6UiState.Error,
    modifier: Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        ResultContent(ResultState.FAILURE)
        Text(stringFromUIText(uiState.message))
    }
}

@Composable
private fun Loading(
    modifier: Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        CircularProgressIndicator()
    }
}
