/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/2/2023.
 */
package com.adyen.checkout.bcmc.internal.ui.model

import com.adyen.checkout.card.internal.ui.model.ExpiryDate
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData

internal data class BcmcOutputData internal constructor(
    val cardNumberField: FieldState<String>,
    val expiryDateField: FieldState<ExpiryDate>,
    val cardHolderNameField: FieldState<String>,
    val isStoredPaymentMethodEnabled: Boolean
) : OutputData {
    override val isValid: Boolean
        get() = (
            cardNumberField.validation.isValid() &&
                expiryDateField.validation.isValid() &&
                cardHolderNameField.validation.isValid()
            )
}
