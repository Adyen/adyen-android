/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 */

package com.adyen.checkout.test.rule

import androidx.test.espresso.IdlingRegistry
import androidx.test.platform.app.InstrumentationRegistry
import com.adyen.checkout.test.util.OkHttpIdlingResource
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

/**
 * Registers an [OkHttpIdlingResource] for the app's [OkHttpClient], so Espresso waits for in flight requests.
 */
class OkHttpIdlingRule : TestRule {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    internal interface OkHttpClientEntryPoint {

        fun okHttpClient(): OkHttpClient
    }

    override fun apply(base: Statement, description: Description?): Statement = object : Statement() {
        override fun evaluate() {
            val application = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
            val okHttpClient = EntryPointAccessors
                .fromApplication(application, OkHttpClientEntryPoint::class.java)
                .okHttpClient()

            val idlingResource = OkHttpIdlingResource(okHttpClient.dispatcher)
            IdlingRegistry.getInstance().register(idlingResource)

            try {
                base.evaluate()
            } finally {
                IdlingRegistry.getInstance().unregister(idlingResource)
            }
        }
    }
}
