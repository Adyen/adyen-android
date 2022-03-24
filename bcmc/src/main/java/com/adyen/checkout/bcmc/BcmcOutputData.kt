/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.bcmc

import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class BcmcOutputData internal constructor(
    val cardNumberField: FieldState<String>,
    val expiryDateField: FieldState<ExpiryDate>,
    val isStoredPaymentMethodEnabled: Boolean
) : OutputData {
    override val isValid: Boolean
        get() = (
            cardNumberField.validation.isValid() &&
                expiryDateField.validation.isValid()
            )
}
