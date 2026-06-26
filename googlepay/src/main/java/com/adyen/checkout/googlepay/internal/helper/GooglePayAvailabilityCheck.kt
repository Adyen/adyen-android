/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/6/2026.
 */

package com.adyen.checkout.googlepay.internal.helper

import android.content.Context
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.Wallet

internal class GooglePayAvailabilityCheck(
    private val componentParams: GooglePayComponentParams,
) {

    suspend fun isAvailable(context: Context): Boolean {
        val playServicesAvailable = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
        if (!playServicesAvailable) {
            adyenLog(AdyenLogLevel.ERROR) { "GooglePay is not available, Google Play Services are missing." }
            return false
        }

        val paymentsClient = Wallet.getPaymentsClient(context, GooglePayUtils.createWalletOptions(componentParams))
        val task = paymentsClient.isReadyToPay(GooglePayUtils.createIsReadyToPayRequest(componentParams)).awaitTask()
        return if (task.isSuccessful) {
            task.result == true
        } else {
            adyenLog(AdyenLogLevel.ERROR, task.exception) { "GooglePay isReadyToPay task failed." }
            false
        }
    }
}
