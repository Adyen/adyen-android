/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/7/2022.
 */

package com.adyen.checkout.components.util

import com.adyen.checkout.core.api.Environment
import org.junit.Assert.assertEquals
import org.junit.Test
import java.net.URL

internal class ValidationUtilsTest {

    @Test
    fun `client key should match environment`() {
        clientKeyEnvironmentSource().forEach {
            val clientKey: String = it[0] as String
            val environment: Environment = it[1] as Environment
            val shouldMatch: Boolean = it[2] as Boolean

            assertEquals(shouldMatch, ValidationUtils.doesClientKeyMatchEnvironment(clientKey, environment))
        }
    }

    companion object {

        private fun clientKeyEnvironmentSource() = listOf(
            arrayOf("test_someclientkey", Environment.TEST, true),
            arrayOf("test_someclientkey", Environment.EUROPE, false),
            arrayOf("test_someclientkey", Environment.UNITED_STATES, false),
            arrayOf("test_someclientkey", Environment.AUSTRALIA, false),
            arrayOf("test_someclientkey", Environment.INDIA, false),
            arrayOf("test_someclientkey", Environment.APSE, false),
            arrayOf("live_someclientkey", Environment.EUROPE, true),
            arrayOf("live_someclientkey", Environment.UNITED_STATES, true),
            arrayOf("live_someclientkey", Environment.AUSTRALIA, true),
            arrayOf("live_someclientkey", Environment.INDIA, true),
            arrayOf("live_someclientkey", Environment.APSE, true),
            arrayOf("live_someclientkey", Environment(URL("https://randomenv.com")), true),
            arrayOf(
                "live_someclientkey",
                Environment(URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/")),
                true
            ),
            arrayOf(
                "test_someclientkey",
                Environment(URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/")),
                false
            ),
        )
    }
}
