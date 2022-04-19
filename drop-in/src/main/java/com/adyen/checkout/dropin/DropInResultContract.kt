/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/8/2021.
 */

package com.adyen.checkout.dropin

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class DropInResultContract : ActivityResultContract<Intent, DropInResult?>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): DropInResult? {
        return handleActivityResult(resultCode, intent)
    }

    private fun handleActivityResult(resultCode: Int, data: Intent?): DropInResult? {
        return when {
            data == null -> null
            resultCode == Activity.RESULT_CANCELED && data.hasExtra(DropIn.ERROR_REASON_KEY) -> {
                val reason = data.getStringExtra(DropIn.ERROR_REASON_KEY) ?: ""
                if (reason == DropIn.ERROR_REASON_USER_CANCELED) DropInResult.CancelledByUser()
                else DropInResult.Error(reason)
            }
            resultCode == Activity.RESULT_OK && data.hasExtra(DropIn.RESULT_KEY) -> {
                DropInResult.Finished(data.getStringExtra(DropIn.RESULT_KEY) ?: "")
            }
            else -> null
        }
    }
}
