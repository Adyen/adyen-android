/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 21/12/2020.
 */

package com.adyen.checkout.card

import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.PaymentMethodDelegate
import com.adyen.checkout.components.validation.ValidatedField

interface CardDelegate : PaymentMethodDelegate {

    fun validateCardNumber(cardNumber: String): ValidatedField<String>
    fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate>
    fun validateSecurityCode(
        securityCode: String,
        cardConfiguration: CardConfiguration,
        cardType: CardType? = null
    ): ValidatedField<String>

    fun validateHolderName(holderName: String, cardConfiguration: CardConfiguration): ValidatedField<String> {
        return if (cardConfiguration.isHolderNameRequire && holderName.isBlank()) {
            ValidatedField(holderName, ValidatedField.Validation.INVALID)
        } else {
            ValidatedField(holderName, ValidatedField.Validation.VALID)
        }
    }

    fun isCvcHidden(cardConfiguration: CardConfiguration) : Boolean
}
