/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.test.util

import androidx.test.espresso.IdlingResource
import okhttp3.Dispatcher

/**
 * Keeps Espresso busy while OkHttp has calls in flight.
 *
 * [IdlingResourceDispatcher] only tracks work that is dispatched on a [kotlinx.coroutines.CoroutineDispatcher].
 * Retrofit's suspending functions enqueue their calls on OkHttp's own thread pool and suspend the coroutine while
 * waiting, which makes the app look idle to Espresso for the whole duration of a request.
 */
internal class OkHttpIdlingResource(
    private val dispatcher: Dispatcher,
) : IdlingResource {

    @Volatile
    private var resourceCallback: IdlingResource.ResourceCallback? = null

    init {
        dispatcher.idleCallback = Runnable { resourceCallback?.onTransitionToIdle() }
    }

    override fun getName(): String = "okhttp"

    override fun isIdleNow(): Boolean = dispatcher.runningCallsCount() == 0

    override fun registerIdleTransitionCallback(callback: IdlingResource.ResourceCallback) {
        resourceCallback = callback
    }
}
