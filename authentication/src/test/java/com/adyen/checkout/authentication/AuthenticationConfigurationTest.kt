/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.authentication

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class AuthenticationConfigurationTest {

    @Test
    fun `when two configurations are created with the same values, then they are equal`() {
        val first = AuthenticationConfiguration(threeDSRequestorAppURL = TEST_APP_URL)
        val second = AuthenticationConfiguration(threeDSRequestorAppURL = TEST_APP_URL)

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `when two configurations are created with different values, then they are not equal`() {
        assertNotEquals(
            AuthenticationConfiguration(threeDSRequestorAppURL = TEST_APP_URL),
            AuthenticationConfiguration(threeDSRequestorAppURL = null),
        )
    }

    companion object {
        private const val TEST_APP_URL = "https://www.adyen.com"
    }
}
