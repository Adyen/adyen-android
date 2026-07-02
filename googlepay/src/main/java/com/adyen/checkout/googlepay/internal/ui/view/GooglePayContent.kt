/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.view

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adyen.checkout.googlepay.internal.ui.GooglePayViewEvent
import com.adyen.checkout.googlepay.internal.ui.state.GooglePayViewState
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

@Composable
internal fun GooglePayContent(
    viewStateFlow: StateFlow<GooglePayViewState>,
    viewEventFlow: Flow<GooglePayViewEvent>,
    onResult: (ApiTaskResult<PaymentData>) -> Unit,
    loadPaymentData: suspend (Context) -> Task<PaymentData>,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult(),
        onResult = onResult,
    )
    LaunchedEffect(viewEventFlow) {
        viewEventFlow.collect { event ->
            when (event) {
                GooglePayViewEvent.Pay -> launcher.launch(loadPaymentData(context))
            }
        }
    }

    val viewState by viewStateFlow.collectAsStateWithLifecycle()
    GooglePayContent(
        viewState = viewState,
        onSubmit = onSubmit,
        modifier = modifier,
    )
}

@Composable
private fun GooglePayContent(
    viewState: GooglePayViewState,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    viewState.buttonViewState?.let { buttonViewState ->
        GooglePayButton(
            buttonViewState = buttonViewState,
            onClick = onSubmit,
            modifier = modifier,
        )
    }
}
