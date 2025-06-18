/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 30/10/2024.
 */

package com.adyen.checkout.googlepay.internal.util

import android.app.Application
import com.adyen.checkout.components.core.ComponentAvailableCallback
import com.adyen.checkout.components.core.PaymentMethod
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.Wallet
import java.lang.ref.WeakReference

internal class GooglePayAvailabilityCheck(
    private val application: Application,
) {

    fun isAvailable(
        paymentMethod: PaymentMethod,
        componentParams: GooglePayComponentParams,
        callback: ComponentAvailableCallback,
    ) {
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(application) != ConnectionResult.SUCCESS
        ) {
            callback.onAvailabilityResult(false, paymentMethod)
            return
        }

        val callbackWeakReference = WeakReference(callback)

        val paymentsClient = Wallet.getPaymentsClient(application, GooglePayUtils.createWalletOptions(componentParams))
        val readyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(componentParams)
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnSuccessListener { result ->
            callbackWeakReference.get()?.onAvailabilityResult(result == true, paymentMethod)
        }
        readyToPayTask.addOnCanceledListener {
            adyenLog(AdyenLogLevel.ERROR) { "GooglePay readyToPay task is cancelled." }
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
        }
        readyToPayTask.addOnFailureListener {
            adyenLog(AdyenLogLevel.ERROR, it) { "GooglePay readyToPay task is failed." }
            callbackWeakReference.get()?.onAvailabilityResult(false, paymentMethod)
        }
    }
}
