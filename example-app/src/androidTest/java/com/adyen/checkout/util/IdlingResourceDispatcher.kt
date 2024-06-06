/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.util

import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

internal class IdlingResourceDispatcher(
    private val wrappedDispatcher: CoroutineDispatcher,
    private val counter: CountingIdlingResource,
) : CoroutineDispatcher() {

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        counter.increment()
        val runnable = Runnable {
            try {
                block.run()
            } finally {
                counter.decrement()
            }
        }
        wrappedDispatcher.dispatch(context, runnable)
    }
}
