/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 15/3/2023.
 */

package com.adyen.checkout.cashapppay

import app.cash.paykit.core.CashAppPayListener
import app.cash.paykit.core.CashAppPayState

internal class DefaultCashAppPayListener(
    private val onCashAppPayStateChanged: (CashAppPayState) -> Unit
) : CashAppPayListener {
    override fun cashAppPayStateDidChange(newState: CashAppPayState) {
        onCashAppPayStateChanged(newState)
    }
}
