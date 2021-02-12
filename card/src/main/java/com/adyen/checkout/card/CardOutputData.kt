/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.validation.ValidatedField

data class CardOutputData(
    val cardNumberField: ValidatedField<String>,
    val expiryDateField: ValidatedField<ExpiryDate>,
    val securityCodeField: ValidatedField<String>,
    val holderNameField: ValidatedField<String>,
    val isStoredPaymentMethodEnable: Boolean,
    val isCvcHidden: Boolean,
    val detectedCardTypes: List<DetectedCardType>
) : OutputData {
    override fun isValid(): Boolean {
        return cardNumberField.isValid &&
            expiryDateField.isValid &&
            securityCodeField.isValid &&
            holderNameField.isValid
    }
}
