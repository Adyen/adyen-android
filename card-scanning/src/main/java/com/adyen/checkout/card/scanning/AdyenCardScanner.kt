/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/12/2024.
 */

package com.adyen.checkout.card.scanning

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.adyenLog
import com.google.android.gms.wallet.PaymentCardRecognitionIntentRequest
import com.google.android.gms.wallet.PaymentCardRecognitionResult
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class AdyenCardScanner {

    private var paymentCardRecognitionPendingIntent: PendingIntent? = null

    suspend fun initialize(context: Context, environment: Environment): Boolean {
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

    private suspend fun initializeCardRecognition(paymentsClient: PaymentsClient): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
            paymentsClient
                .getPaymentCardRecognitionIntent(request)
                .addOnSuccessListener { intentResponse ->
                    if (continuation.isActive) {
                        paymentCardRecognitionPendingIntent = intentResponse.paymentCardRecognitionPendingIntent
                        continuation.resume(true)
                    }
                }
                .addOnFailureListener { e ->
                    adyenLog(AdyenLogLevel.WARN, e) { "Card scanning not available" }
                    if (continuation.isActive) {
                        continuation.resume(false)
                    }
                }
                .addOnCanceledListener {
                    if (continuation.isActive) {
                        continuation.cancel()
                    }
                }
        }
    }

    fun startScanner(activity: Activity, requestCode: Int): Boolean {
        return startScanner { paymentCardRecognitionPendingIntent ->
            ActivityCompat.startIntentSenderForResult(
                activity,
                paymentCardRecognitionPendingIntent.intentSender,
                requestCode,
                null,
                0,
                0,
                0,
                null,
            )
        }
    }

    fun startScanner(fragment: Fragment, requestCode: Int): Boolean {
        return startScanner { paymentCardRecognitionPendingIntent ->
            fragment.startIntentSenderForResult(
                paymentCardRecognitionPendingIntent.intentSender,
                requestCode,
                null,
                0,
                0,
                0,
                null,
            )
        }
    }

    private fun startScanner(startIntentSender: (PendingIntent) -> Unit): Boolean {
        val paymentCardRecognitionPendingIntent =
            paymentCardRecognitionPendingIntent ?: error("The scanner must be initialized before it can be started")

        return try {
            startIntentSender(paymentCardRecognitionPendingIntent)
            true
        } catch (e: IntentSender.SendIntentException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to start payment card recognition" }
            false
        }
    }

    fun getResult(data: Intent?): AdyenCardScannerResult? {
        data ?: return null
        return PaymentCardRecognitionResult.getFromIntent(data)
            ?.let { result ->
                AdyenCardScannerResult(
                    result.pan,
                    result.creditCardExpirationDate?.month,
                    result.creditCardExpirationDate?.year,
                )
            }
    }

    fun terminate() {
        paymentCardRecognitionPendingIntent = null
    }
}
