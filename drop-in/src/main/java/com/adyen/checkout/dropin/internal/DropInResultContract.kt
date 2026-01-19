/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/11/2025.
 */

package com.adyen.checkout.dropin.internal

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.activity.result.contract.ActivityResultContract
import com.adyen.checkout.core.common.CheckoutContext
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog
import com.adyen.checkout.dropin.DropInResult
import com.adyen.checkout.dropin.DropInService
import com.adyen.checkout.dropin.internal.ui.DropInActivity
import kotlinx.parcelize.Parcelize

internal class DropInResultContract : ActivityResultContract<DropInResultContract.Input, DropInResult>() {

    override fun createIntent(context: Context, input: Input): Intent {
        return Intent(context, DropInActivity::class.java)
            .putExtra(EXTRA_INPUT, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): DropInResult {
        @Suppress("DEPRECATION")
        return intent?.getParcelableExtra<Result>(EXTRA_RESULT)?.dropInResult ?: run {
            adyenLog(AdyenLogLevel.ERROR) { "No result extra found" }
            // TODO - check if it's okay to return Failed, because this state does not mean the payment failed
            DropInResult.Failed("No DropInResult available")
        }
    }

    @Parcelize
    internal data class Input(
        val checkoutContext: CheckoutContext,
        val serviceClass: Class<out DropInService>,
    ) : Parcelable {

        companion object {

            @Suppress("DEPRECATION")
            fun from(intent: Intent): Input? {
                return intent.getParcelableExtra(EXTRA_INPUT)
            }
        }
    }

    @Parcelize
    internal data class Result(
        val dropInResult: DropInResult,
    ) : Parcelable

    companion object {
        private const val EXTRA_INPUT = "com.adyen.checkout.dropin.internal.DropInResultContract.EXTRA_INPUT"

        private const val EXTRA_RESULT = "com.adyen.checkout.dropin.internal.DropInResultContract.EXTRA_RESULT"
    }
}
