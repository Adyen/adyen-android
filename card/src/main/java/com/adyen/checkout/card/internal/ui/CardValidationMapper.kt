/*
 * Copyright (c) 2021 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 5/10/2021.
 */

package com.adyen.checkout.card.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.R
import com.adyen.checkout.card.internal.util.CardNumberValidation
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class CardValidationMapper {

    fun mapCardNumberValidation(cardNumber: String, validation: CardNumberValidation): FieldState<String> {
        val fieldStateValidation = when (validation) {
            CardNumberValidation.INVALID_ILLEGAL_CHARACTERS ->
                Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.INVALID_TOO_SHORT -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.INVALID_TOO_LONG -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.INVALID_UNSUPPORTED_BRAND -> Validation.Invalid(
                reason = R.string.checkout_card_brand_not_supported,
                showErrorWhileEditing = true
            )
            CardNumberValidation.INVALID_LUHN_CHECK -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.VALID -> Validation.Valid
        }

        return FieldState(cardNumber, fieldStateValidation)
    }
}
