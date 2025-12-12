/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 5/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

internal interface StoredCardChangeListener {

    fun onSecurityCodeChanged(newSecurityCode: String)

    fun onSecurityCodeFocusChanged(hasFocus: Boolean)
}
