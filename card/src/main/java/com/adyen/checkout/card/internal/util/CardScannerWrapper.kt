/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 21/5/2026.
 */

package com.adyen.checkout.card.internal.util

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.adyen.checkout.card.scanning.internal.CardScannerController
import com.adyen.checkout.card.scanning.internal.CardScannerInitializer
import com.adyen.checkout.core.common.Environment
import com.adyen.checkout.core.common.helper.runCompileOnly

internal class CardScannerWrapper {

    private var controller: CardScannerController? = null

    suspend fun initialize(context: Context, environment: Environment): Boolean {
        // we can't use getOrNull() here because CardScannerController is defined inside the card-scanning module
        // so the JVM will try to resolve the type outside runCompileOnly which will throw a NoClassDefFoundError
        runCompileOnly {
            controller = CardScannerInitializer.initialize(context, environment)
            return controller != null
        }

        // runCompileOnly didn't return which means the card-scanning module is not included
        controller = null
        return false
    }

    fun getIntentSender(): IntentSender? {
        return controller?.getIntentSender()
    }

    fun parseResult(intent: Intent?): ScanResult? {
        val result = controller?.parseResult(intent) ?: return null
        return ScanResult(
            pan = result.pan,
            expiryMonth = result.expiryMonth,
            expiryYear = result.expiryYear,
        )
    }

    internal data class ScanResult(
        val pan: String?,
        val expiryMonth: Int?,
        val expiryYear: Int?,
    )
}
