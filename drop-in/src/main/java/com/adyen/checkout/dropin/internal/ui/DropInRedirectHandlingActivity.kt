/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 24/8/2026.
 */

package com.adyen.checkout.dropin.internal.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.annotation.RestrictTo
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog

/**
 * Receives the deep link the shopper is redirected back to and forwards it to [DropInActivity], which is
 * a `singleTask` activity and therefore gets it through [DropInActivity.onNewIntent].
 *
 * This thin forwarder exists so that [DropInActivity] itself does not have to be exported.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DropInRedirectHandlingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val redirectIntent = intent
        if (redirectIntent == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Received a null intent" }
            finish()
            return
        }

        adyenLog(AdyenLogLevel.INFO) { "Forwarding intent to DropInActivity" }
        startActivity(
            Intent(this, DropInActivity::class.java).apply { fillIn(redirectIntent, 0) },
        )
        finish()
    }
}
