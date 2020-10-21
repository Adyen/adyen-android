/*
 * Copyright (c) 2019 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by arman on 16/10/2019.
 */

package com.adyen.checkout.example.rules

import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.IdlingResource
import com.jakewharton.espresso.OkHttp3IdlingResource
import okhttp3.OkHttpClient
import org.junit.rules.TestRule
import org.junit.runners.model.Statement

class OkHttpIdlingResourceRule(client: OkHttpClient) : TestRule {

    private val resource: IdlingResource = OkHttp3IdlingResource.create("okhttp", client)

    override fun apply(base: Statement?, description: org.junit.runner.Description?): Statement {
        return object : Statement() {
            override fun evaluate() {
                IdlingRegistry.getInstance().register(resource)
                base?.evaluate()
                IdlingRegistry.getInstance().unregister(resource)
            }
        }
    }
}
