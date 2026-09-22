/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 22/9/2026.
 */

package com.adyen.checkout.core.action.internal.ui

import com.adyen.checkout.core.action.data.RedirectAction
import com.adyen.checkout.core.components.paymentmethod.PaymentMethodTypes
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ActionFormatterTest {

    @Test
    fun `when the action belongs to a card, then the generic card logo is used`() {
        val action = createAction(paymentMethodType = PaymentMethodTypes.SCHEME)

        assertEquals("card", ActionFormatter.getIcon(action))
    }

    // Card-like, but it has a logo of its own, so it must not be swapped for the generic card one.
    @Test
    fun `when the action belongs to bcmc, then its own logo is used`() {
        val action = createAction(paymentMethodType = PaymentMethodTypes.BCMC)

        assertEquals("bcmc", ActionFormatter.getIcon(action))
    }

    @Test
    fun `when the action belongs to any other payment method, then its own logo is used`() {
        val action = createAction(paymentMethodType = PaymentMethodTypes.IDEAL)

        assertEquals("ideal", ActionFormatter.getIcon(action))
    }

    @Test
    fun `when the action names no payment method, then there is no logo to load`() {
        val action = createAction(paymentMethodType = null)

        assertEquals("", ActionFormatter.getIcon(action))
    }

    private fun createAction(paymentMethodType: String?) = RedirectAction(
        type = RedirectAction.ACTION_TYPE,
        paymentData = null,
        paymentMethodType = paymentMethodType,
        method = null,
        url = null,
        nativeRedirectData = null,
    )
}
