/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 3/2/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.view

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.adyen.checkout.googlepay.internal.helper.GooglePayUtils
import com.adyen.checkout.googlepay.internal.helper.awaitTask
import com.adyen.checkout.googlepay.internal.ui.GooglePayViewEvent
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.contract.ApiTaskResult
import com.google.android.gms.wallet.contract.TaskResultContracts
import kotlinx.coroutines.flow.Flow

@Composable
internal fun GooglePayContent(
    componentParams: GooglePayComponentParams,
    viewEventFlow: Flow<GooglePayViewEvent>,
    onResult: (ApiTaskResult<PaymentData>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val paymentsClient = remember(context) {
        Wallet.getPaymentsClient(context, GooglePayUtils.createWalletOptions(componentParams))
    }
    val launcher = rememberLauncherForActivityResult(
        contract = TaskResultContracts.GetPaymentDataResult(),
        onResult = onResult,
    )
    LaunchedEffect(viewEventFlow) {
        viewEventFlow.collect { event ->
            when (event) {
                GooglePayViewEvent.Pay -> {
                    val task = paymentsClient.loadPaymentData(
                        GooglePayUtils.createPaymentDataRequest(componentParams),
                    ).awaitTask()
                    launcher.launch(task)
                }
            }
        }
    }

    // TODO - Render the Google Pay button.
    Box(modifier = modifier)
}
