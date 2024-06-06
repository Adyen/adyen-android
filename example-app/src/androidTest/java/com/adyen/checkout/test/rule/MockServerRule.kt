/*
 * Copyright (c) 2024 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 6/6/2024.
 */

package com.adyen.checkout.test.rule

import com.adyen.checkout.test.server.CheckoutMockWebServer
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class MockServerRule : TestRule {

    override fun apply(base: Statement, description: Description?): Statement = object : Statement() {
        override fun evaluate() {
            try {
                CheckoutMockWebServer.start()
                base.evaluate()
            } finally {
                CheckoutMockWebServer.stop()
            }
        }
    }
}
