/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.googlepay.compose

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
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
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.example.ui.googlepay.GooglePayActivityResult
import com.adyen.checkout.googlepay.GooglePayComponent
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

@Composable
internal fun SessionsGooglePayScreen(
    useDarkTheme: Boolean,
    onBackPressed: () -> Unit,
    viewModel: SessionsGooglePayViewModel,
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(text = "Google Pay with sessions") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val googlePayState by viewModel.googlePayState.collectAsState()
        SessionsGooglePayContent(
            googlePayState = googlePayState,
            onButtonClicked = viewModel::onButtonClicked,
            useDarkTheme = useDarkTheme,
            modifier = Modifier.padding(innerPadding),
        )

        with(googlePayState) {
            HandleActivityResult(activityResult, componentData, viewModel::onActivityResultHandled)
            HandleAction(action, componentData, viewModel::onActionConsumed)
            HandleNewIntent(newIntent, componentData, viewModel::onNewIntentHandled)
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun SessionsGooglePayContent(
    googlePayState: SessionsGooglePayState,
    onButtonClicked: () -> Unit,
    useDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        when (val uiState = googlePayState.uiState) {
            SessionsGooglePayUIState.Loading -> {
                CircularProgressIndicator()
            }

            SessionsGooglePayUIState.ShowButton -> {
                val activity = LocalContext.current as Activity
                val googlePayComponent = getGooglePayComponent(componentData = googlePayState.componentData)
                PayButton(
                    onClick = {
                        googlePayComponent.startGooglePayScreen(
                            activity,
                            SessionsGooglePayActivity.ACTIVITY_RESULT_CODE,
                        )
                        onButtonClicked()
                    },
                    allowedPaymentMethods = googlePayComponent.getGooglePayButtonParameters().allowedPaymentMethods,
                    theme = if (useDarkTheme) ButtonTheme.Light else ButtonTheme.Dark,
                    type = ButtonType.Pay,
                )
            }

            SessionsGooglePayUIState.ShowComponent -> {
                val googlePayComponent = getGooglePayComponent(componentData = googlePayState.componentData)
                AdyenComponent(
                    googlePayComponent,
                    modifier,
                )
            }

            is SessionsGooglePayUIState.FinalResult -> {
                ResultContent(uiState.finalResult)
            }
        }
    }
}

@Composable
private fun HandleActivityResult(
    activityResult: GooglePayActivityResult?,
    componentData: SessionsGooglePayComponentData?,
    onActivityResultHandled: () -> Unit
) {
    if (activityResult == null) return
    val googlePayComponent = getGooglePayComponent(componentData = componentData)
    LaunchedEffect(activityResult) {
        googlePayComponent.handleActivityResult(activityResult.resultCode, activityResult.data)
        onActivityResultHandled()
    }
}

@Composable
private fun HandleAction(
    action: Action?,
    componentData: SessionsGooglePayComponentData?,
    onActionConsumed: () -> Unit
) {
    if (action == null) return
    val activity = LocalContext.current as Activity
    val googlePayComponent = getGooglePayComponent(componentData = componentData)
    LaunchedEffect(action) {
        googlePayComponent.handleAction(action, activity)
        onActionConsumed()
    }
}

@Composable
private fun HandleNewIntent(
    newIntent: Intent?,
    componentData: SessionsGooglePayComponentData?,
    onNewIntentHandled: () -> Unit
) {
    if (newIntent == null) return
    val googlePayComponent = getGooglePayComponent(componentData = componentData)
    LaunchedEffect(newIntent) {
        googlePayComponent.handleIntent(newIntent)
        onNewIntentHandled()
    }
}

@Composable
private fun getGooglePayComponent(componentData: SessionsGooglePayComponentData?): GooglePayComponent {
    requireNotNull(componentData) { "Component data should not be null" }
    return with(componentData) {
        GooglePayComponent.PROVIDER.get(
            checkoutSession,
            paymentMethod,
            googlePayConfiguration,
            callback,
            hashCode().toString(),
        )
    }
}
