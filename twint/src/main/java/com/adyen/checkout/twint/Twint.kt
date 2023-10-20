/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/11/2023.
 */

package com.adyen.checkout.twint

import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import ch.twint.payment.sdk.Twint
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.core.exception.CheckoutException

object Twint {

    private var twint: Twint? = null

    private var onResultListener: ((TwintPayResult) -> Unit)? = null

    fun initialize(activity: ComponentActivity) {
        twint = Twint(activity, ::onTwintResult)

        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onDestroy()
                super.onDestroy(owner)
            }
        }
        activity.lifecycle.addObserver(observer)
    }

    private fun onTwintResult(result: TwintPayResult) {
        onResultListener?.invoke(result)
    }

    internal fun setResultListener(listener: (TwintPayResult) -> Unit) {
        onResultListener = listener
    }

    internal fun payWithCode(code: String) {
        twint?.payWithCode(code) ?: throw CheckoutException("Twint not initialised before payment.")
    }

    private fun onDestroy() {
        onResultListener = null
        twint = null
    }
}
