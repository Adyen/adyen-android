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
import com.adyen.checkout.card.internal.util.CardExpiryDateValidation
import com.adyen.checkout.card.internal.util.CardNumberValidation
import com.adyen.checkout.card.internal.util.CardSecurityCodeValidation
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.internal.util.StringUtil

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
                showErrorWhileEditing = true,
            )

            CardNumberValidation.INVALID_LUHN_CHECK -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.INVALID_OTHER_REASON -> Validation.Invalid(R.string.checkout_card_number_not_valid)
            CardNumberValidation.VALID -> Validation.Valid
        }

        return FieldState(cardNumber, fieldStateValidation)
    }

    fun mapExpiryDateValidation(
        expiryDate: String,
        validationResult: CardExpiryDateValidation,
    ): FieldState<String> {
        val validation = when (validationResult) {
            CardExpiryDateValidation.VALID -> Validation.Valid
            CardExpiryDateValidation.VALID_NOT_REQUIRED -> Validation.Valid
            CardExpiryDateValidation.INVALID_TOO_FAR_IN_THE_FUTURE ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_far_in_future)

            CardExpiryDateValidation.INVALID_TOO_OLD ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_old)

            CardExpiryDateValidation.INVALID_OTHER_REASON -> Validation.Invalid(R.string.checkout_expiry_date_not_valid)
        }
        return FieldState(expiryDate, validation)
    }

    fun mapSecurityCodeValidation(
        securityCode: String,
        validationResult: CardSecurityCodeValidation
    ): FieldState<String> {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)

        val validation = when (validationResult) {
            CardSecurityCodeValidation.VALID -> Validation.Valid
            CardSecurityCodeValidation.VALID_HIDDEN -> Validation.Valid
            CardSecurityCodeValidation.VALID_OPTIONAL_EMPTY -> Validation.Valid
            CardSecurityCodeValidation.INVALID -> Validation.Invalid(R.string.checkout_security_code_not_valid)
        }

        return FieldState(normalizedSecurityCode, validation)
    }
}
