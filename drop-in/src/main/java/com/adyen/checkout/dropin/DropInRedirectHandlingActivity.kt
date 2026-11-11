/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/11/2026.
 */

package com.adyen.checkout.dropin

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.adyen.checkout.core.common.AdyenLogLevel
import com.adyen.checkout.core.common.internal.helper.adyenLog
import com.adyen.checkout.dropin.internal.ui.DropInActivity

/**
 * Receives the return from a redirect, for example from the shopper's bank, and passes it on to Drop-in.
 *
 * Drop-in declares no intent filter for redirects. To receive them, declare this activity in your manifest with
 * `android:exported="true"` and an intent filter matching the `returnUrl` you send to Checkout API. Use
 * [DropIn.setRedirectHandlingEnabled] to turn it on and off at runtime.
 *
 * Only the action and URL of the incoming intent are passed on, nothing else.
 */
class DropInRedirectHandlingActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val data = intent.data
        if (intent.action != Intent.ACTION_VIEW || data == null) {
            adyenLog(AdyenLogLevel.ERROR) { "Ignoring intent that is not a redirect return: action=${intent.action}" }
        } else {
            // A new intent rather than the incoming one, so no extras or flags from outside reach Drop-in
            startActivity(Intent(this, DropInActivity::class.java).setAction(Intent.ACTION_VIEW).setData(data))
        }
        finish()
    }
}
