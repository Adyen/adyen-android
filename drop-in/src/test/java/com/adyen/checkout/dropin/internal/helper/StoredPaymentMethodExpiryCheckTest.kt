/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 22/10/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.Calendar
import java.util.GregorianCalendar

internal class StoredPaymentMethodExpiryCheckTest {

    // 22 October 2026
    private val expiryCheck = StoredPaymentMethodExpiryCheck { GregorianCalendar(2026, Calendar.OCTOBER, 22) }

    @Test
    fun `when the card expires in a later month, then it is not expired`() {
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "11", expiryYear = "2026")))
    }

    @Test
    fun `when the card expires this month, then it is not expired`() {
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "10", expiryYear = "2026")))
    }

    @Test
    fun `when the card expired last month, then it is expired`() {
        assertTrue(expiryCheck.isExpired(storedCard(expiryMonth = "09", expiryYear = "2026")))
    }

    @Test
    fun `when the card expired in an earlier year, then it is expired`() {
        assertTrue(expiryCheck.isExpired(storedCard(expiryMonth = "12", expiryYear = "2025")))
    }

    @Test
    fun `when the expiry year has two digits, then it is read as this century`() {
        assertTrue(expiryCheck.isExpired(storedCard(expiryMonth = "09", expiryYear = "26")))
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "11", expiryYear = "26")))
    }

    @Test
    fun `when the expiry date can't be read, then the card is not expired`() {
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "13", expiryYear = "2020")))
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "1", expiryYear = "2020")))
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "01", expiryYear = "020")))
        assertFalse(expiryCheck.isExpired(storedCard(expiryMonth = "", expiryYear = "")))
    }

    @Test
    fun `when the stored payment method is not a card, then it is not expired`() {
        val storedPaymentMethod = StoredBLIKPaymentMethod(
            type = PaymentMethodTypes.BLIK,
            name = "BLIK",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        assertFalse(expiryCheck.isExpired(storedPaymentMethod))
    }

    private fun storedCard(expiryMonth: String, expiryYear: String) = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        name = "Visa",
        id = "id",
        supportedShopperInteractions = emptyList(),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = expiryMonth,
        expiryYear = expiryYear,
        holderName = null,
        fundingSource = null,
    )
}
