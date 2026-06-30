/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 26/6/2026.
 */

package com.adyen.checkout.googlepay.internal.ui.view

import com.adyen.checkout.googlepay.GooglePayButtonTheme
import com.adyen.checkout.googlepay.GooglePayButtonType
import com.google.pay.button.ButtonTheme
import com.google.pay.button.ButtonType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class GooglePayButtonStylingMapperTest {

    @Test
    fun `when toButtonTheme is called with LIGHT, then it maps to ButtonTheme Light`() {
        assertEquals(ButtonTheme.Light, GooglePayButtonTheme.LIGHT.toButtonTheme())
    }

    @Test
    fun `when toButtonTheme is called with DARK, then it maps to ButtonTheme Dark`() {
        assertEquals(ButtonTheme.Dark, GooglePayButtonTheme.DARK.toButtonTheme())
    }

    @Test
    fun `when toButtonTheme is called with null, then it defaults to ButtonTheme Dark`() {
        assertEquals(ButtonTheme.Dark, (null as GooglePayButtonTheme?).toButtonTheme())
    }

    @Test
    fun `when toButtonType is called with each type, then it maps to the matching ButtonType`() {
        assertEquals(ButtonType.Buy, GooglePayButtonType.BUY.toButtonType())
        assertEquals(ButtonType.Book, GooglePayButtonType.BOOK.toButtonType())
        assertEquals(ButtonType.Checkout, GooglePayButtonType.CHECKOUT.toButtonType())
        assertEquals(ButtonType.Donate, GooglePayButtonType.DONATE.toButtonType())
        assertEquals(ButtonType.Order, GooglePayButtonType.ORDER.toButtonType())
        assertEquals(ButtonType.Pay, GooglePayButtonType.PAY.toButtonType())
        assertEquals(ButtonType.Subscribe, GooglePayButtonType.SUBSCRIBE.toButtonType())
        assertEquals(ButtonType.Plain, GooglePayButtonType.PLAIN.toButtonType())
    }

    @Test
    fun `when toButtonType is called with null, then it defaults to ButtonType Buy`() {
        assertEquals(ButtonType.Buy, (null as GooglePayButtonType?).toButtonType())
    }
}
