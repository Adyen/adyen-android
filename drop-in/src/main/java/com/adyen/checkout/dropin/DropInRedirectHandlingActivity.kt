/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 23/4/2025.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adyen.checkout.core.AdyenLogLevel
import com.adyen.checkout.core.internal.util.adyenLog
import com.adyen.checkout.dropin.internal.ui.DropInActivity

class DropInRedirectHandlingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        adyenLog(AdyenLogLevel.DEBUG) { "onCreate" }
        super.onCreate(savedInstanceState)

        val intent = intent
        if (intent == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Received a null intent" }
            return
        }

        val newIntent = Intent(this, DropInActivity::class.java).apply {
            fillIn(intent, 0)
        }
        adyenLog(AdyenLogLevel.INFO) { "Forwarding intent to DropInActivity" }
        startActivity(newIntent)
        finish()
    }
}
