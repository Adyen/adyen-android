/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 2/11/2026.
 */

package com.adyen.checkout.card

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

internal class BillingAddressModeTest {

    @Test
    fun `when two None instances are compared, then they are equal`() {
        assertEquals(BillingAddressMode.None(), BillingAddressMode.None())
        assertEquals(BillingAddressMode.None().hashCode(), BillingAddressMode.None().hashCode())
    }

    @Test
    fun `when two PostalCode instances are compared, then they are equal`() {
        assertEquals(BillingAddressMode.PostalCode(), BillingAddressMode.PostalCode())
        assertEquals(BillingAddressMode.PostalCode().hashCode(), BillingAddressMode.PostalCode().hashCode())
    }

    @Test
    fun `when different modes are compared, then they are not equal`() {
        val none: BillingAddressMode = BillingAddressMode.None()
        val postalCode: BillingAddressMode = BillingAddressMode.PostalCode()

        assertNotEquals(none, postalCode)
    }
}
