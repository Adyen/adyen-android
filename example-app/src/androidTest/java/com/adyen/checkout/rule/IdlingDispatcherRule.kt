/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.rule

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.idling.CountingIdlingResource
import com.adyen.checkout.util.IdlingResourceDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class IdlingDispatcherRule : TestRule {

    override fun apply(base: Statement?, description: Description?): Statement = object : Statement() {
        override fun evaluate() {
            // Main dispatcher is not needed as this is not doing background work
            val defaultDispatcher = Dispatchers.Default
            val ioDispatcher = Dispatchers.IO

            val defaultIdlingResource = CountingIdlingResource("default")
            val ioIdlingResource = CountingIdlingResource("io")

            IdlingRegistry.getInstance().apply {
                register(defaultIdlingResource)
                register(ioIdlingResource)
            }

            val defaultIdlingDispatcher = IdlingResourceDispatcher(defaultDispatcher, defaultIdlingResource)
            val ioIdlingDispatcher = IdlingResourceDispatcher(ioDispatcher, ioIdlingResource)

            overrideDispatchers(defaultIdlingDispatcher, ioIdlingDispatcher)

            try {
                base?.evaluate()
            } finally {
                IdlingRegistry.getInstance().apply {
                    unregister(defaultIdlingResource)
                    unregister(ioIdlingResource)
                }
                overrideDispatchers(defaultDispatcher, ioDispatcher)
            }
        }
    }

    private fun overrideDispatchers(
        default: CoroutineDispatcher,
        io: CoroutineDispatcher,
    ) {
        fun setField(name: String, value: CoroutineDispatcher) {
            Dispatchers::class.java.getDeclaredField(name).apply {
                isAccessible = true
                set(Dispatchers, value)
            }
        }

        setField("Default", default)
        setField("IO", io)
    }
}
