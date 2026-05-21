/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/5/2026.
 */

package com.adyen.checkout.card.scanning.internal

import android.content.Context
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.google.android.gms.wallet.PaymentCardRecognitionIntentRequest
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
object CardScannerInitializer {

    suspend fun initialize(context: Context, environment: Environment): CardScannerController? {
        val paymentsClient = createPaymentsClient(context, environment)
        return initializeCardRecognition(paymentsClient)
    }

    private fun createPaymentsClient(context: Context, environment: Environment): PaymentsClient {
        val googleEnv = if (environment == Environment.TEST) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }

        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(googleEnv)
            .build()

        return Wallet.getPaymentsClient(context, walletOptions)
    }

    private suspend fun initializeCardRecognition(paymentsClient: PaymentsClient): CardScannerController? {
        return suspendCancellableCoroutine { continuation ->
            val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
            paymentsClient
                .getPaymentCardRecognitionIntent(request)
                .addOnSuccessListener { intentResponse ->
                    if (continuation.isActive) {
                        continuation.resume(
                            CardScannerController(intentResponse.paymentCardRecognitionPendingIntent),
                        )
                    }
                }
                .addOnFailureListener { e ->
                    adyenLog(
                        AdyenLogLevel.WARN,
                        TAG,
                        e,
                    ) { "Card scanning not available" }
                    if (continuation.isActive) {
                        continuation.resume(null)
                    }
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.cancel()
                    }
                }
        }
    }

    private const val TAG = "CardScannerInitializer"
}
