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
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.googlepay.GooglePayComponent
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

@Composable
internal fun SessionsGooglePayScreen(
    useDarkTheme: Boolean,
    onBackPressed: () -> Unit,
    viewModel: SessionsGooglePayViewModel,
) {
    val googlePayState by viewModel.googlePayState.collectAsState()
    val events by viewModel.stateEvents.collectAsState()
    val googlePayLauncher = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult(),
        onResult = viewModel::onGooglePayLauncherResult,
    )

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
            HandleStartGooglePay(startGooglePay, googlePayLauncher, viewModel::onGooglePayStarted)
            HandlePaymentResult(paymentResultToHandle, viewModel::onPaymentResultHandled)
            HandleAction(actionToHandle, viewModel::onActionConsumed)
            HandleNewIntent(intentToHandle, viewModel::onNewIntentHandled)
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
        when (googlePayState) {
            SessionsGooglePayState.Loading -> {
                CircularProgressIndicator()
            }

            is SessionsGooglePayState.ShowButton -> {
                PayButton(
                    onClick = onButtonClicked,
                    allowedPaymentMethods = googlePayComponent.getGooglePayButtonParameters().allowedPaymentMethods,
                    theme = if (useDarkTheme) ButtonTheme.Light else ButtonTheme.Dark,
                    type = ButtonType.Pay,
                )
            }

            is SessionsGooglePayState.ShowComponent -> {
                AdyenComponent(
                    googlePayComponent,
                    modifier,
                )
            }

            is SessionsGooglePayState.FinalResult -> {
                ResultContent(googlePayState.finalResult)
            }
        }
    }
}

@Composable
private fun HandleStartGooglePay(
    startGooglePayData: SessionsStartGooglePayData?,
    googlePayLauncher: ManagedActivityResultLauncher<Task<PaymentData>, ApiTaskResult<PaymentData>>,
    onGooglePayStarted: () -> Unit
) {
    if (startGooglePayData == null) return
    val googlePayComponent = getGooglePayComponent(componentData = startGooglePayData.componentData)
    LaunchedEffect(startGooglePayData) {
        googlePayComponent.startGooglePayScreen(googlePayLauncher)
        onGooglePayStarted()
    }
}

@Composable
private fun HandlePaymentResult(
    paymentResultToHandle: SessionsGooglePayPaymentResult?,
    onPaymentResultHandled: () -> Unit
) {
    if (paymentResultToHandle == null) return
    val googlePayComponent = getGooglePayComponent(componentData = paymentResultToHandle.componentData)
    LaunchedEffect(paymentResultToHandle) {
        googlePayComponent.handlePaymentResult(paymentResultToHandle.paymentDataTaskResult)
        onPaymentResultHandled()
    }
}

@Composable
private fun HandleAction(
    actionToHandle: SessionsGooglePayAction?,
    onActionConsumed: () -> Unit
) {
    if (actionToHandle == null) return
    val activity = LocalContext.current as Activity
    val googlePayComponent = getGooglePayComponent(componentData = actionToHandle.componentData)
    LaunchedEffect(actionToHandle) {
        googlePayComponent.handleAction(actionToHandle.action, activity)
        onActionConsumed()
    }
}

@Composable
private fun HandleNewIntent(
    intentToHandle: SessionsGooglePayIntent?,
    onNewIntentHandled: () -> Unit
) {
    if (intentToHandle == null) return
    val googlePayComponent = getGooglePayComponent(componentData = intentToHandle.componentData)
    LaunchedEffect(intentToHandle) {
        googlePayComponent.handleIntent(intentToHandle.intent)
        onNewIntentHandled()
    }
}

@Composable
private fun getGooglePayComponent(componentData: SessionsGooglePayComponentData): GooglePayComponent {
    return with(componentData) {
        GooglePayComponent.PROVIDER.get(
            checkoutSession,
            paymentMethod,
            checkoutConfiguration,
            callback,
            hashCode().toString(),
        )
    }
}
