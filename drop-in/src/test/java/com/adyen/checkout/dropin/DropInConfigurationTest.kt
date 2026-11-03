/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.dropin

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class DropInConfigurationTest {

    @Test
    fun `when two configurations are created with the same values, then they are equal`() {
        val first = createConfiguration()
        val second = createConfiguration()

        assertEquals(first, second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun `when two configurations are created with different values, then they are not equal`() {
        assertNotEquals(createConfiguration(), createConfiguration(hideStoredPaymentMethods = false))
    }

    private fun createConfiguration(
        hideStoredPaymentMethods: Boolean = true,
    ) = DropInConfiguration(
        hideStoredPaymentMethods = hideStoredPaymentMethods,
        startWithLastStoredPaymentMethod = false,
    )
}
