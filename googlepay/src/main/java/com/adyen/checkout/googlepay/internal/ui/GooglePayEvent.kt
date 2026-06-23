/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 23/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import kotlinx.coroutines.flow.Flow

@SuppressLint("ComposableNaming")
@Composable
internal fun googlePayEvent(
    componentParams: GooglePayComponentParams,
    viewEventFlow: Flow<GooglePayViewEvent>,
    onResult: (ApiTaskResult<PaymentData>) -> Unit,
) {
    val context = LocalContext.current
    val paymentsClient = remember(context, componentParams) {
        Wallet.getPaymentsClient(context, GooglePayUtils.createWalletOptions(componentParams))
    }
    val launcher = rememberLauncherForActivityResult(TaskResultContracts.GetPaymentDataResult(), onResult)
    LaunchedEffect(viewEventFlow) {
        viewEventFlow.collect { event ->
            when (event) {
                GooglePayViewEvent.Pay -> {
                    val task = paymentsClient.loadPaymentData(
                        GooglePayUtils.createPaymentDataRequest(componentParams),
                    )
                    task.addOnCompleteListener(launcher::launch)
                }
            }
        }
    }
}
