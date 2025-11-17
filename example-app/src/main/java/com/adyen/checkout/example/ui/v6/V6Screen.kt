/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 12/8/2025.
 */

package com.adyen.checkout.example.ui.v6

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adyen.checkout.core.components.AdyenPaymentFlow
import com.adyen.checkout.core.components.data.model.PaymentMethod
import com.adyen.checkout.example.ui.compose.ResultContent
import com.adyen.checkout.example.ui.compose.ResultState
import com.adyen.checkout.example.ui.compose.stringFromUIText
import com.adyen.checkout.example.ui.theme.ExampleTheme
import com.adyen.checkout.ui.internal.Body
import com.adyen.checkout.ui.internal.SubHeadline
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxSize(),
    ) {
        var shouldShowDialog by remember { mutableStateOf(false) }
        var selectedPaymentMethod by rememberSaveable { mutableStateOf(uiState.paymentMethods.first()) }

        DropDownButton(
            theme = theme,
            onClick = { shouldShowDialog = !shouldShowDialog },
            text = selectedPaymentMethod.name.orEmpty(),
            isExpanded = shouldShowDialog,
        )

        if (shouldShowDialog) {
            PaymentMethodOptionsDialog(
                paymentMethods = uiState.paymentMethods,
                onItemClick = { selectedPaymentMethod = it },
                onDismissRequest = { shouldShowDialog = false },
                theme = theme,
            )
        }

        AdyenPaymentFlow(
            txVariant = selectedPaymentMethod.type ?: error("Payment method has no type"),
            checkoutContext = uiState.checkoutContext,
            checkoutCallbacks = uiState.checkoutCallbacks,
            theme = theme,
            modifier = Modifier.padding(ExampleTheme.dimensions.grid_2),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropDownButton(
    theme: CheckoutTheme,
    onClick: () -> Unit,
    text: String,
    isExpanded: Boolean,
) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(ExampleTheme.dimensions.grid_4))
            .border(
                width = 1.dp,
                color = Color(theme.colors.outline.value),
                shape = RoundedCornerShape(ExampleTheme.dimensions.grid_4),
            )
            .clickable(onClick = onClick)
            .padding(
                start = ExampleTheme.dimensions.grid_2,
                top = ExampleTheme.dimensions.grid_1,
                end = ExampleTheme.dimensions.grid_1,
                bottom = ExampleTheme.dimensions.grid_1,
            ),
    ) {
        Text(text)
        ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
    }
}

@Composable
private fun PaymentMethodOptionsDialog(
    paymentMethods: List<PaymentMethod>,
    onItemClick: (PaymentMethod) -> Unit,
    onDismissRequest: () -> Unit,
    theme: CheckoutTheme,
) {
    Dialog(
        onDismissRequest,
    ) {
        Surface(
            shape = RoundedCornerShape(ExampleTheme.dimensions.grid_1),
            modifier = Modifier.padding(vertical = ExampleTheme.dimensions.grid_2),
        ) {
            LazyColumn(
                modifier = Modifier.padding(vertical = ExampleTheme.dimensions.grid_1),
            ) {
                items(paymentMethods) { paymentMethod ->
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                onItemClick(paymentMethod)
                                onDismissRequest()
                            }
                            .padding(
                                horizontal = ExampleTheme.dimensions.grid_2,
                                vertical = ExampleTheme.dimensions.grid_1_5,
                            ),
                    ) {
                        @Suppress("RestrictedApi")
                        Body(paymentMethod.name.orEmpty())
                        @Suppress("RestrictedApi")
                        SubHeadline(paymentMethod.type.orEmpty(), color = Color(theme.colors.textSecondary.value))
                    }
                }
            }
        }
    }
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
