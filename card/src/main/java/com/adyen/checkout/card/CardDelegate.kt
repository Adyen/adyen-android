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

abstract class CardDelegate(protected val cardConfiguration: CardConfiguration) : PaymentMethodDelegate {

    abstract fun validateCardNumber(cardNumber: String): ValidatedField<String>
    abstract fun validateExpiryDate(expiryDate: ExpiryDate): ValidatedField<ExpiryDate>
    abstract fun validateSecurityCode(securityCode: String, cardType: CardType? = null): ValidatedField<String>

    fun validateHolderName(holderName: String): ValidatedField<String> {
        return if (cardConfiguration.isHolderNameRequired && holderName.isBlank()) {
            ValidatedField(holderName, ValidatedField.Validation.INVALID)
        } else {
            ValidatedField(holderName, ValidatedField.Validation.VALID)
        }
    }

    abstract fun isCvcHidden(): Boolean
    abstract fun requiresInput(): Boolean
    abstract fun isHolderNameRequired(): Boolean
}
