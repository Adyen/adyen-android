/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 14/1/2025.
 */

package com.adyen.checkout.blik.internal.ui.state

import com.adyen.checkout.core.components.data.PaymentComponentData
import com.adyen.checkout.core.components.data.model.Amount
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.paymentmethod.BlikPaymentMethod
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class BlikComponentStateExtTest {

    @Test
    fun `when toPaymentComponentState is called, then a valid component state is created`() {
        val amount = Amount("PLN", 1000)
        val componentState = BlikComponentState(
            blikCode = TextInputComponentState(
                text = "123456",
            ),
            isLoading = false,
        )

        val paymentComponentState = componentState.toPaymentComponentState(
            amount = amount,
        )

        val expectedPaymentMethod = BlikPaymentMethod(
            type = BlikPaymentMethod.PAYMENT_METHOD_TYPE,
            blikCode = "123456",
            storedPaymentMethodId = null,
        )
        val expectedPaymentComponentData = PaymentComponentData(
            paymentMethod = expectedPaymentMethod,
            order = null,
            amount = amount,
        )
        val expectedPaymentComponentState = BlikPaymentComponentState(
            data = expectedPaymentComponentData,
            isValid = true,
        )

        assertEquals(expectedPaymentComponentState, paymentComponentState)
        assertTrue(paymentComponentState.isValid)
    }
}
