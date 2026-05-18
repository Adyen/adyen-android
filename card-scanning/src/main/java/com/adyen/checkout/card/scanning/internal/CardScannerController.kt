/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/5/2026.
 */

package com.adyen.checkout.card.scanning.internal

import android.app.PendingIntent
import android.content.Intent
import android.content.IntentSender
import androidx.annotation.RestrictTo
import com.google.android.gms.wallet.PaymentCardRecognitionResult

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardScannerController internal constructor(
    pendingIntent: PendingIntent,
) {

    private var paymentCardRecognitionPendingIntent: PendingIntent? = pendingIntent

    fun getIntentSender(): IntentSender {
        val pendingIntent = paymentCardRecognitionPendingIntent
            ?: error("CardScannerController has been terminated.")
        return pendingIntent.intentSender
    }

    fun parseResult(intent: Intent?): CardScanResult? {
        intent ?: return null
        return PaymentCardRecognitionResult.getFromIntent(intent)
            ?.let { result ->
                CardScanResult(
                    pan = result.pan,
                    expiryMonth = result.creditCardExpirationDate?.month,
                    expiryYear = result.creditCardExpirationDate?.year,
                )
            }
    }

    fun terminate() {
        paymentCardRecognitionPendingIntent = null
    }
}
