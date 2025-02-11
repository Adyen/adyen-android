/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 29/11/2023.
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.adyen.checkout.example.ui.card.compose

import android.widget.Toast
import androidx.activity.compose.LocalActivity
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
import com.adyen.checkout.components.compose.AdyenComponent
import com.adyen.checkout.components.compose.get
import com.adyen.checkout.components.core.AddressLookupCallback
import com.adyen.checkout.components.core.AddressLookupResult
import com.adyen.checkout.components.core.CheckoutConfiguration
import com.adyen.checkout.components.core.LookupAddress
import com.adyen.checkout.components.core.action.Action
import com.adyen.checkout.example.ui.card.SessionsCardComponentData
import com.adyen.checkout.example.ui.card.SessionsCardUiState
import com.adyen.checkout.example.ui.card.SessionsCardViewModel
import com.adyen.checkout.example.ui.compose.ResultContent

@Composable
internal fun SessionsCardScreen(
    onBackPressed: () -> Unit,
    viewModel: SessionsCardViewModel = hiltViewModel(),
) {
    Scaffold(
        modifier = Modifier.windowInsetsPadding(WindowInsets.ime),
        topBar = {
            TopAppBar(
                title = { Text(text = "Card component with sessions") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { innerPadding ->
        val uiState by viewModel.uiState.collectAsState()
        SessionsCardContent(
            uiState = uiState,
            onOneTimeMessageConsumed = viewModel::oneTimeMessageConsumed,
            onActionConsumed = viewModel::actionConsumed,
            addressLookupCallback = viewModel as AddressLookupCallback,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Suppress("DestructuringDeclarationWithTooManyEntries")
@Composable
private fun SessionsCardContent(
    uiState: SessionsCardUiState,
    onOneTimeMessageConsumed: () -> Unit,
    onActionConsumed: () -> Unit,
    addressLookupCallback: AddressLookupCallback,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        val (
            checkoutConfiguration,
            isLoading,
            oneTimeMessage,
            componentData,
            action,
            addressLookupOptions,
            addressLookupResult,
            finalResult
        ) = uiState

        if (isLoading) {
            CircularProgressIndicator()
        }

        if (oneTimeMessage != null) {
            val context = LocalContext.current
            LaunchedEffect(oneTimeMessage) {
                Toast.makeText(context, oneTimeMessage, Toast.LENGTH_SHORT).show()
                onOneTimeMessageConsumed()
            }
        }

        if (finalResult != null) {
            ResultContent(finalResult)
        } else if (componentData != null) {
            CardComponent(
                checkoutConfiguration = checkoutConfiguration,
                componentData = componentData,
                action = action,
                onActionConsumed = onActionConsumed,
                addressLookupCallback = addressLookupCallback,
                addressLookupOptions = addressLookupOptions,
                addressLookupResult = addressLookupResult,
                modifier = Modifier.fillMaxSize(),
            )
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun CardComponent(
    checkoutConfiguration: CheckoutConfiguration,
    componentData: SessionsCardComponentData,
    action: Action?,
    onActionConsumed: () -> Unit,
    addressLookupCallback: AddressLookupCallback,
    addressLookupOptions: List<LookupAddress>,
    addressLookupResult: AddressLookupResult?,
    modifier: Modifier = Modifier,
) {
    val component = CardComponent.PROVIDER.get(
        componentData.checkoutSession,
        componentData.paymentMethod,
        checkoutConfiguration,
        componentData.callback,
        componentData.hashCode().toString(),
    )

    if (addressLookupOptions.isNotEmpty()) {
        component.updateAddressLookupOptions(addressLookupOptions)
    }

    if (addressLookupResult != null) {
        component.setAddressLookupResult(addressLookupResult)
    }

    component.setAddressLookupCallback(addressLookupCallback)

    // Enables vertical scrolling when the CardView becomes too long.
    Column(modifier.verticalScroll(rememberScrollState())) {
        AdyenComponent(
            component,
            modifier,
        )
    }

    val activity = LocalActivity.current
    if (activity != null && action != null) {
        LaunchedEffect(action) {
            component.handleAction(action, activity)
            onActionConsumed()
        }
    }
}
