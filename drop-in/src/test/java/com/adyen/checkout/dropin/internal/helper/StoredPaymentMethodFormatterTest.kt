/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.components.data.model.paymentmethod.StoredACHDirectDebitPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCashAppPayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredInstantPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayToPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

internal class StoredPaymentMethodFormatterTest {

    @Test
    fun `when type is scheme, then icon is brand`() {
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            brand = "brand",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )

        val result = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod)

        assertEquals("brand", result)
    }

    @Test
    fun `when type is not scheme, then icon is type`() {
        val storedPaymentMethod = StoredInstantPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        val result = StoredPaymentMethodFormatter.getIcon(storedPaymentMethod)

        assertEquals(PaymentMethodTypes.ACH, result)
    }

    @Test
    fun `when type is cash app pay, then title is cashtag`() {
        val storedPaymentMethod = StoredCashAppPayPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            cashtag = "cashtag",
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("cashtag", result)
    }

    @Test
    fun `when type is pay by bank us, then title is label`() {
        val storedPaymentMethod = StoredPayByBankUSPaymentMethod(
            type = PaymentMethodTypes.PAY_BY_BANK_US,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            label = "label",
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("label", result)
    }

    @Test
    fun `when type is pay to, then title is label`() {
        val storedPaymentMethod = StoredPayToPaymentMethod(
            type = PaymentMethodTypes.PAY_TO,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            label = "label",
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("label", result)
    }

    // TODO - COSDK-998: Create StoredPayPalPaymentMethod with shopperEmail field and test it here
    @Test
    fun `when type is paypal, then title is name`() {
        val storedPaymentMethod = StoredInstantPaymentMethod(
            type = PaymentMethodTypes.PAYPAL,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is ach, then title shows last four digits of account number`() {
        val storedPaymentMethod = StoredACHDirectDebitPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            bankAccountNumber = "123456789",
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("•••• 6789", result)
    }

    @Test
    fun `when type is scheme, then title is lastFour`() {
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("•••• 1234", result)
    }

    @Test
    fun `when type is other, then title is name`() {
        val storedPaymentMethod = StoredInstantPaymentMethod(
            type = "other",
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        val result = StoredPaymentMethodFormatter.getTitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is ach, then subtitle is name`() {
        val storedPaymentMethod = StoredACHDirectDebitPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            bankAccountNumber = "123456789",
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is cash app pay, then subtitle is name`() {
        val storedPaymentMethod = StoredCashAppPayPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            cashtag = "cashtag",
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    // TODO - COSDK-998: Create StoredPayPalPaymentMethod with shopperEmail field and test it here
    @Test
    fun `when type is paypal, then subtitle is null`() {
        val storedPaymentMethod = StoredInstantPaymentMethod(
            type = PaymentMethodTypes.PAYPAL,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertNull(result)
    }

    @Test
    fun `when type is pay by bank us, then subtitle is name`() {
        val storedPaymentMethod = StoredPayByBankUSPaymentMethod(
            type = PaymentMethodTypes.PAY_BY_BANK_US,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            label = "label",
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is pay to, then subtitle is name`() {
        val storedPaymentMethod = StoredPayToPaymentMethod(
            type = PaymentMethodTypes.PAY_TO,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            label = "label",
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is scheme, then subtitle is name`() {
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            brand = "mc",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertEquals("name", result)
    }

    @Test
    fun `when type is other, then subtitle is null`() {
        val storedPaymentMethod = StoredBLIKPaymentMethod(
            type = "other",
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
        )

        val result = StoredPaymentMethodFormatter.getSubtitle(storedPaymentMethod)

        assertNull(result)
    }
}
