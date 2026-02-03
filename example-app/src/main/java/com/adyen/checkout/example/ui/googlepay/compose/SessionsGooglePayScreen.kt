/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 13/12/2023.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.googlepay.compose

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.googlepay.old.GooglePayComponent

@Composable
internal fun SessionsGooglePayScreen(
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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        SessionsGooglePayContent(
            googlePayState = googlePayState,
            googlePayEvents = eventsState,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Suppress("LongParameterList")
@Composable
private fun SessionsGooglePayContent(
    googlePayState: SessionsGooglePayState,
    googlePayEvents: SessionsGooglePayEvents,
    modifier: Modifier = Modifier,
) {
    val activity = LocalActivity.current
    lateinit var googlePayComponent: GooglePayComponent

    when (googlePayEvents) {
        is SessionsGooglePayEvents.ComponentData -> {
            googlePayComponent = getGooglePayComponent(componentData = googlePayEvents.data)
        }

        is SessionsGooglePayEvents.Action -> {
            LaunchedEffect(googlePayEvents.action) {
                if (activity != null) {
                    googlePayComponent.handleAction(googlePayEvents.action, activity)
                }
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
                AdyenComponent(googlePayComponent)
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
