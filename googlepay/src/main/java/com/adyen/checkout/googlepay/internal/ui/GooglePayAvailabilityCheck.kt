/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 26/6/2024.
 */

package com.adyen.checkout.googlepay.internal.ui

import android.content.Context
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.googlepay.internal.ui.model.GooglePayComponentParams
import com.adyen.checkout.googlepay.internal.util.GooglePayUtils
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.wallet.PaymentsClient
import java.lang.ref.WeakReference

internal class GooglePayAvailabilityCheck {

    operator fun invoke(
        context: Context,
        componentParams: GooglePayComponentParams,
        paymentsClient: PaymentsClient,
        callback: (Boolean) -> Unit,
    ) {
        val googlePlayServicesAvailability = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context)
        if (googlePlayServicesAvailability != ConnectionResult.SUCCESS) {
            callback(false)
            return
        }

        val callbackWeakReference = WeakReference(callback)

        val readyToPayRequest = GooglePayUtils.createIsReadyToPayRequest(componentParams)
        val readyToPayTask = paymentsClient.isReadyToPay(readyToPayRequest)
        readyToPayTask.addOnSuccessListener { result ->
            callbackWeakReference.get()?.invoke(result == true)
        }
        readyToPayTask.addOnCanceledListener {
            adyenLog(AdyenLogLevel.ERROR) { "GooglePay readyToPay task is cancelled." }
            callbackWeakReference.get()?.invoke(false)
        }
        readyToPayTask.addOnFailureListener {
            adyenLog(AdyenLogLevel.ERROR, it) { "GooglePay readyToPay task is failed." }
            callbackWeakReference.get()?.invoke(false)
        }
    }
}
