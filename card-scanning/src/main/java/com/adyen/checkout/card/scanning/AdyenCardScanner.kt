/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/12/2024.
 */

package com.adyen.checkout.card.scanning

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.adyen.checkout.core.Environment

class AdyenCardScanner {

    fun initialize(context: Context, environment: Environment) {

    }

    suspend fun isAvailable(): Boolean {
        return true
    }

    fun startScanner(activity: Activity) {

    }

    fun getResult(data: Intent?): AdyenCardScannerResult? {
        return null
    }

    fun terminate() {

    }
}

data class AdyenCardScannerResult(
    val pan: String?,
    val expiryMonth: Int?,
    val expiryYear: Int?,
)
