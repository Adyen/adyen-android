/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 2/8/2021.
 */

package com.adyen.checkout.dropin

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

internal class DropInResultContract : ActivityResultContract<Intent, DropInResult?>() {
    override fun createIntent(context: Context, input: Intent): Intent {
        return input
    }

    override fun parseResult(resultCode: Int, intent: Intent?): DropInResult? {
        return DropIn.handleActivityResult(DropIn.DROP_IN_REQUEST_CODE, resultCode, intent)
    }
}
