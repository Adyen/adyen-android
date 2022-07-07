/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 7/7/2022.
 */

package com.adyen.checkout.components.util

import com.adyen.checkout.core.api.Environment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URL

internal class ValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("clientKeyEnvironmentSource")
    fun `client key should match environment`(clientKey: String, environment: Environment, shouldMatch: Boolean) {
        assertEquals(shouldMatch, ValidationUtils.doesClientKeyMatchEnvironment(clientKey, environment))
    }

    companion object {

        @JvmStatic
        fun clientKeyEnvironmentSource() = listOf(
            arguments("test_someclientkey", Environment.TEST, true),
            arguments("test_someclientkey", Environment.EUROPE, false),
            arguments("test_someclientkey", Environment.UNITED_STATES, false),
            arguments("test_someclientkey", Environment.AUSTRALIA, false),
            arguments("test_someclientkey", Environment.INDIA, false),
            arguments("test_someclientkey", Environment.APSE, false),
            arguments("live_someclientkey", Environment.EUROPE, true),
            arguments("live_someclientkey", Environment.UNITED_STATES, true),
            arguments("live_someclientkey", Environment.AUSTRALIA, true),
            arguments("live_someclientkey", Environment.INDIA, true),
            arguments("live_someclientkey", Environment.APSE, true),
            arguments("live_someclientkey", Environment(URL("https://randomenv.com")), true),
            arguments("live_someclientkey", Environment(URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/")), true),
            arguments("test_someclientkey", Environment(URL("https://checkoutshopper-live-us.adyen.com/checkoutshopper/")), false),
        )
    }
}
