/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.StoredPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class StoredPaymentMethodFormatterTest {

    @Test
    fun `when type is scheme, then icon is brand`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            brand = "brand"
        )

        val result = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod)

        assertEquals("brand", result)
    }

    @Test
    fun `when type is scheme and brand is null, then icon is empty`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            brand = null
        )

        val result = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod)

        assertEquals("", result)
    }

    @Test
    fun `when type is not scheme, then icon is type`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            brand = "brand"
        )

        val result = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod)

        assertEquals(PaymentMethodTypes.ACH, result)
    }

    @Test
    fun `when type is cash app pay, then title is cashtag`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "name",
            cashtag = "cashtag"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("cashtag", result)
    }

    @Test
    fun `when type is cash app pay and cashtag is null, then title is empty`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "name",
            cashtag = null
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("", result)
    }

    @Test
    fun `when type is pay by bank us, then title is label`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAY_BY_BANK_US,
            name = "name",
            label = "label"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("label", result)
    }

    @Test
    fun `when type is pay to, then title is label`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAY_TO,
            name = "name",
            label = "label"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("label", result)
    }

    @Test
    fun `when type is paypal, then title is shopperEmail`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAYPAL,
            name = "name",
            shopperEmail = "shopperEmail"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("shopperEmail", result)
    }

    @Test
    fun `when type is ach, then title is lastFour`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            lastFour = "1234"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("•••• 1234", result)
    }

    @Test
    fun `when type is ach and lastFour is null, then title is formatted correctly`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            lastFour = null
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("•••• ", result)
    }

    @Test
    fun `when type is scheme, then title is lastFour`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            lastFour = "1234"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("•••• 1234", result)
    }

    @Test
    fun `when type is other, then title is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = "other",
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is ach, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is cash app pay, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is paypal, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAYPAL,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is pay by bank us, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAY_BY_BANK_US,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is pay to, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.PAY_TO,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is scheme, then subtitle is name`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is other, then subtitle is null`() {
        val storedPaymentMethod = StoredPaymentMethod(
            type = "other",
            name = "name"
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertNull(result)
    }
}
