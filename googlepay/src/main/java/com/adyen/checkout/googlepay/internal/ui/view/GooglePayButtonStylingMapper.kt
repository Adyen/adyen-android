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

internal fun GooglePayButtonTheme?.toButtonTheme(): ButtonTheme = when (this) {
    GooglePayButtonTheme.LIGHT -> ButtonTheme.Light
    GooglePayButtonTheme.DARK,
    null -> ButtonTheme.Dark
}

internal fun GooglePayButtonType?.toButtonType(): ButtonType = when (this) {
    GooglePayButtonType.BOOK -> ButtonType.Book
    GooglePayButtonType.CHECKOUT -> ButtonType.Checkout
    GooglePayButtonType.DONATE -> ButtonType.Donate
    GooglePayButtonType.ORDER -> ButtonType.Order
    GooglePayButtonType.PAY -> ButtonType.Pay
    GooglePayButtonType.SUBSCRIBE -> ButtonType.Subscribe
    GooglePayButtonType.PLAIN -> ButtonType.Plain
    GooglePayButtonType.BUY,
    null -> ButtonType.Buy
}
