/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 9/10/2025.
 */

package com.adyen.checkout.card.internal.ui.state

internal interface CardChangeListener {

    fun onCardNumberChanged(newCardNumber: String)

    fun onCardNumberFocusChanged(hasFocus: Boolean)

    fun onExpiryDateChanged(newExpiryDate: String)

    fun onExpiryDateFocusChanged(hasFocus: Boolean)

    fun onSecurityCodeChanged(newSecurityCode: String)

    fun onSecurityCodeFocusChanged(hasFocus: Boolean)
}
