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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.googlepay.GooglePayComponent
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.TaskResultContracts
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import com.google.pay.button.PayButton

@Composable
internal fun SessionsGooglePayScreen(
    useDarkTheme: Boolean,
    googlePayState: SessionsGooglePayState,
    eventsState: SessionsGooglePayEvents,
    onBackPressed: () -> Unit,
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
        SessionsGooglePayContent(
            googlePayState = googlePayState,
            googlePayEvents = eventsState,
            useDarkTheme = useDarkTheme,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun SessionsGooglePayContent(
    googlePayState: SessionsGooglePayState,
    googlePayEvents: SessionsGooglePayEvents,
    useDarkTheme: Boolean,
    modifier: Modifier = Modifier,
) {
    val activity = LocalContext.current as Activity

    lateinit var googlePayComponent: GooglePayComponent
    lateinit var googlePayLauncher: ActivityResultLauncher<Task<PaymentData>>

    when (googlePayEvents) {
        is SessionsGooglePayEvents.ComponentData -> {
            googlePayComponent = getGooglePayComponent(componentData = googlePayEvents.data)
            googlePayLauncher = rememberLauncherForActivityResult(
                contract = TaskResultContracts.GetPaymentDataResult(),
                onResult = googlePayComponent::handlePaymentResult,
            )
        }
        is SessionsGooglePayEvents.Action -> {
            LaunchedEffect(googlePayEvents.action) {
                googlePayComponent.handleAction(googlePayEvents.action, activity)
            }
        }
        is SessionsGooglePayEvents.Intent -> {
            LaunchedEffect(googlePayEvents.intent) {
                googlePayComponent.handleIntent(googlePayEvents.intent)
            }
        }
    }

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
                    onClick = {
                        googlePayComponent.startGooglePayScreen(googlePayLauncher)
                    },
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
