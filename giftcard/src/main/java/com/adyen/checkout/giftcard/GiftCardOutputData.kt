/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 9/9/2021.
 */

package com.adyen.checkout.giftcard

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.giftcard.util.GiftCardNumberUtils
import com.adyen.checkout.giftcard.util.GiftCardPinUtils

class GiftCardOutputData(cardNumber: String, pin: String) : OutputData {

    val giftcardNumberFieldState: FieldState<String> = GiftCardNumberUtils.validateInputField(cardNumber)
    val giftcardPinFieldState: FieldState<String> = GiftCardPinUtils.validateInputField(pin)

    override val isValid: Boolean
        get() = giftcardNumberFieldState.validation.isValid() && giftcardPinFieldState.validation.isValid()
}
