/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/11/2023.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.card.compose

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.adyen.checkout.card.CardComponent
import com.adyen.checkout.card.CardConfiguration
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.ui.card.SessionsCardComponentData
import com.adyen.checkout.example.ui.card.SessionsCardViewModel
import com.adyen.checkout.example.ui.compose.ResultContent

@Composable
internal fun SessionsCardScreen(
    onBackPressed: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(text = "Card component with sessions") },
                navigationIcon = {
                    IconButton(onClick = { onBackPressed() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        MainContent(Modifier.padding(innerPadding))
    }
}

@Composable
private fun MainContent(
    modifier: Modifier = Modifier,
    viewModel: SessionsCardViewModel = hiltViewModel(),
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val uiState by viewModel.uiState.collectAsState()

        val (cardConfiguration, isLoading, toastMessage, componentData, action, finalResult) = uiState

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (toastMessage != null) {
            Toast.makeText(LocalContext.current, toastMessage, Toast.LENGTH_SHORT).show()
            viewModel.toastMessageConsumed()
        }

        if (finalResult != null) {
            ResultContent(finalResult)
        } else if (componentData != null) {
            CardComponent(
                configuration = cardConfiguration,
                componentData = componentData,
                action = action,
                onActionConsumed = viewModel::actionConsumed,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Composable
private fun CardComponent(
    configuration: CardConfiguration,
    componentData: SessionsCardComponentData,
    action: Action?,
    onActionConsumed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val component = CardComponent.PROVIDER.get(
        componentData.checkoutSession,
        componentData.paymentMethod,
        configuration,
        componentData.callback,
        componentData.hashCode().toString(),
    )

    // Enables vertical scrolling when the CardView becomes too long.
    Column(modifier.verticalScroll(rememberScrollState())) {
        AdyenComponent(
            component,
            modifier,
        )
    }

    if (action != null) {
        val activity = LocalContext.current as Activity
        LaunchedEffect(action) {
            component.handleAction(action, activity)
            onActionConsumed()
        }
    }
}
