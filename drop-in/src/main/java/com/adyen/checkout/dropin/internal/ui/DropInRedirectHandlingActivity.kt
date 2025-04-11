/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 11/4/2025.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog

class DropInRedirectHandlingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
        super.onCreate(savedInstanceState)

        val intent = intent
        if (intent == null) {
            adyenLog(AdyenLogLevel.DEBUG) { "intent is null" }
            return
        }

        val newIntent = Intent(this, DropInActivity::class.java).apply {
            fillIn(intent, 0)
        }
        startActivity(newIntent)
    }

    override fun onNewIntent(intent: Intent) {
        adyenLog(AdyenLogLevel.DEBUG) { "onNewIntent" }
        super.onNewIntent(intent)
    }

    override fun onDestroy() {
        adyenLog(AdyenLogLevel.DEBUG) { "onDestroy" }
        super.onDestroy()
    }
}
