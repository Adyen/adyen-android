/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import kotlinx.coroutines.flow.Flow

@SuppressLint("ComposableNaming")
@Composable
internal fun googlePayEvent(
    viewEventFlow: Flow<GooglePayViewEvent>,
    onPaymentResult: (ApiTaskResult<PaymentData>) -> Unit,
) {
    val googlePayLauncher = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult(),
    ) { result ->
        onPaymentResult(result)
    }

    LaunchedEffect(viewEventFlow, onPaymentResult) {
        viewEventFlow.collect { event ->
            when (event) {
                is GooglePayViewEvent.LaunchGooglePay -> {
                    googlePayLauncher.launch(event.task)
                }
            }
        }
    }
}
