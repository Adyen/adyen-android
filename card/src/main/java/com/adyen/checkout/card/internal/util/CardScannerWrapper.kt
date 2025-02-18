/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 18/2/2025.
 */

package com.adyen.checkout.card.internal.util

import android.content.Context
import android.content.Intent
import androidx.fragment.app.Fragment
import com.adyen.checkout.card.scanning.AdyenCardScanner
import com.adyen.checkout.card.scanning.AdyenCardScannerResult
import com.adyen.checkout.core.Environment
import com.adyen.checkout.core.internal.util.runCompileOnly

internal class CardScannerWrapper {

    private val cardScanner: AdyenCardScanner? = runCompileOnly { AdyenCardScanner() }

    fun initialize(context: Context, environment: Environment) {
        cardScanner?.initialize(context, environment)
    }

    suspend fun isAvailable(): Boolean {
        return cardScanner?.isAvailable() ?: false
    }

    fun startScanner(fragment: Fragment, requestCode: Int) {
        cardScanner?.startScanner(fragment, requestCode)
    }

    fun getResult(data: Intent?): AdyenCardScannerResult? {
        return cardScanner?.getResult(data)
    }

    fun terminate() {
        cardScanner?.terminate()
    }
}
