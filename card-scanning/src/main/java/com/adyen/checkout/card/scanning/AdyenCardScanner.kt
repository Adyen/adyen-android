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
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.core.internal.util.adyenLog
import com.google.android.gms.wallet.PaymentCardRecognitionIntentRequest
import com.google.android.gms.wallet.PaymentCardRecognitionResult
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.WalletConstants
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AdyenCardScanner {

    private var paymentsClient: PaymentsClient? = null
    private var isAvailable = false
    private var paymentCardRecognitionPendingIntent: PendingIntent? = null

    fun initialize(context: Context, environment: Environment) {
        val googleEnv = if (environment == Environment.TEST) {
            WalletConstants.ENVIRONMENT_TEST
        } else {
            WalletConstants.ENVIRONMENT_PRODUCTION
        }

        val walletOptions = Wallet.WalletOptions.Builder()
            .setEnvironment(googleEnv)
            .build()

        paymentsClient = Wallet.getPaymentsClient(context, walletOptions)
    }

    suspend fun isAvailable(): Boolean {
        val paymentsClient = paymentsClient ?: error("initialize must be called before checking availability")

        return suspendCoroutine { continuation ->
            val request = PaymentCardRecognitionIntentRequest.getDefaultInstance()
            paymentsClient
                .getPaymentCardRecognitionIntent(request)
                .addOnSuccessListener { intentResponse ->
                    paymentCardRecognitionPendingIntent = intentResponse.paymentCardRecognitionPendingIntent
                    continuation.resume(true)
                }
                .addOnFailureListener { e ->
                    adyenLog(AdyenLogLevel.WARN, e) { "Card scanning not available" }
                    continuation.resume(false)
                }
        }
    }

    fun startScanner(activity: Activity, requestCode: Int) {
        val paymentCardRecognitionPendingIntent =
            paymentCardRecognitionPendingIntent ?: error("isAvailable must be called before starting the scanner")

        try {
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
        } catch (e: IntentSender.SendIntentException) {
            throw CheckoutException("Failed to start payment card recognition.", e)
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
        paymentsClient = null
        isAvailable = false
        paymentCardRecognitionPendingIntent = null
    }
}
