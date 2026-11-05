/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/2/2026.
 */

package com.adyen.checkout.dropin.internal.helper

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredACHDirectDebitPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredBLIKPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCardPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredCashAppPayPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayByBankUSPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredPayToPaymentMethod
import com.adyen.checkout.core.components.data.model.paymentmethod.StoredUnsupportedPaymentMethod
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import com.adyen.checkout.dropin.internal.ui.UiText
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.util.Locale

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
        val storedPaymentMethod = StoredACHDirectDebitPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "name",
            id = "id",
            supportedShopperInteractions = emptyList(),
            bankAccountNumber = "123456789",
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

    @Test
    fun `when type is unsupported, then title is name`() {
        val storedPaymentMethod = StoredUnsupportedPaymentMethod(
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
        val storedPaymentMethod = StoredBLIKPaymentMethod(
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

    @Test
    fun `when type is unsupported, then subtitle is null`() {
        val storedPaymentMethod = StoredUnsupportedPaymentMethod(
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

    @Test
    fun `when type is scheme, then removal title is name and last four`() {
        val storedPaymentMethod = StoredCardPaymentMethod(
            type = PaymentMethodTypes.SCHEME,
            name = "Visa",
            id = "id",
            supportedShopperInteractions = emptyList(),
            brand = "visa",
            lastFour = "1234",
            expiryMonth = "01",
            expiryYear = "2030",
            holderName = null,
            fundingSource = null,
        )

        val result = StoredPaymentMethodFormatter.getRemovalTitle(storedPaymentMethod)

        assertEquals("Visa •••• 1234", result)
    }

    @Test
    fun `when type is ach, then removal title is name and last four digits of account number`() {
        val storedPaymentMethod = StoredACHDirectDebitPaymentMethod(
            type = PaymentMethodTypes.ACH,
            name = "ACH",
            id = "id",
            supportedShopperInteractions = emptyList(),
            bankAccountNumber = "123456789",
        )

        val result = StoredPaymentMethodFormatter.getRemovalTitle(storedPaymentMethod)

        assertEquals("ACH •••• 6789", result)
    }

    @Test
    fun `when type is cash app pay, then removal title is name`() {
        val storedPaymentMethod = StoredCashAppPayPaymentMethod(
            type = PaymentMethodTypes.CASH_APP_PAY,
            name = "Cash App Pay",
            id = "id",
            supportedShopperInteractions = emptyList(),
            cashtag = "cashtag",
        )

        val result = StoredPaymentMethodFormatter.getRemovalTitle(storedPaymentMethod)

        assertEquals("Cash App Pay", result)
    }

    @Test
    fun `when amount is null, then description asks to pay without an amount`() {
        val result = StoredPaymentMethodFormatter.getDescription(createStoredCard(), null, Locale.US)

        assertEquals(
            UiText.Localized(CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION, listOf("Visa")),
            result,
        )
    }

    @Test
    fun `when amount is zero, then description asks to save details`() {
        val result = StoredPaymentMethodFormatter.getDescription(createStoredCard(), Amount("USD", 0L), Locale.US)

        assertEquals(
            UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_SAVE_DETAILS,
                listOf("Visa"),
            ),
            result,
        )
    }

    @Test
    fun `when amount is positive, then description asks to pay the formatted amount`() {
        val result = StoredPaymentMethodFormatter.getDescription(createStoredCard(), Amount("USD", 999L), Locale.US)

        assertEquals(
            UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_WITH_AMOUNT,
                listOf("Visa", "$9.99"),
            ),
            result,
        )
    }

    @Test
    fun `when amount is negative, then description asks to pay the formatted amount`() {
        val result = StoredPaymentMethodFormatter.getDescription(createStoredCard(), Amount("USD", -999L), Locale.US)

        assertEquals(
            UiText.Localized(
                CheckoutLocalizationKey.DROPIN_STORED_PAYMENT_METHOD_DESCRIPTION_WITH_AMOUNT,
                listOf("Visa", "-$9.99"),
            ),
            result,
        )
    }

    private fun createStoredCard() = StoredCardPaymentMethod(
        type = PaymentMethodTypes.SCHEME,
        name = "Visa",
        id = "id",
        supportedShopperInteractions = emptyList(),
        brand = "visa",
        lastFour = "1234",
        expiryMonth = "01",
        expiryYear = "2030",
        holderName = null,
        fundingSource = null,
    )
}
