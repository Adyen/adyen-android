/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 17/11/2023.
 */

package com.adyen.checkout.twint

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import ch.twint.payment.sdk.Twint
import ch.twint.payment.sdk.TwintPayResult
import com.adyen.checkout.components.core.action.SdkAction
import com.adyen.checkout.components.core.action.TwintSdkData
import com.adyen.checkout.core.exception.CheckoutException
import com.adyen.checkout.twint.Twint.initialize

/**
 * Object used to manage the Twint SDK. Call [initialize] in [ComponentActivity.onCreate] to make sure [SdkAction]s
 * with [TwintSdkData] can be handled.
 */
object Twint {

    private var twintObject: Twint? = null

    private var onResultListener: ((TwintPayResult) -> Unit)? = null

    /**
     * Initializes the [Twint] object. This method **must** be called in [ComponentActivity.onCreate] or before, because
     * the Twint SDK creates an [ActivityResultLauncher] under the hood.
     */
    fun initialize(activity: ComponentActivity) {
        twintObject = Twint(activity, ::onTwintResult)
        observeLifecycle(activity.lifecycle)
    }

    /**
     * Initializes the [Twint] object. This method **must** be called in [Fragment.onCreateView] or before, because
     * the Twint SDK creates an [ActivityResultLauncher] under the hood.
     */
    fun initialize(fragment: Fragment) {
        twintObject = Twint(fragment, ::onTwintResult)
        observeLifecycle(fragment.lifecycle)
    }

    private fun observeLifecycle(lifecycle: Lifecycle) {
        val observer = object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                onDestroy()
                super.onDestroy(owner)
            }
        }
        lifecycle.addObserver(observer)
    }

    private fun onTwintResult(result: TwintPayResult) {
        onResultListener?.invoke(result)
    }

    internal fun setResultListener(listener: (TwintPayResult) -> Unit) {
        onResultListener = listener
    }

    internal fun payWithCode(code: String) {
        twintObject?.payWithCode(code) ?: throw CheckoutException("Twint not initialised before payment.")
    }

    private fun onDestroy() {
        onResultListener = null
        twintObject = null
    }
}
