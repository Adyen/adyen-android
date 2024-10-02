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
import com.adyen.checkout.card.internal.data.model.Brand
import com.adyen.checkout.card.internal.ui.model.InputFieldUIState
import com.adyen.checkout.card.internal.util.CardNumberValidation
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.internal.util.StringUtil
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.core.ui.validation.CardExpiryDateValidationResult
import com.adyen.checkout.core.ui.validation.CardSecurityCodeValidationResult

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
            CardNumberValidation.VALID -> Validation.Valid
        }

        return FieldState(cardNumber, fieldStateValidation)
    }

    fun mapExpiryDateValidation(
        expiryDate: ExpiryDate,
        fieldPolicy: Brand.FieldPolicy?,
        validationResult: CardExpiryDateValidationResult
    ): FieldState<ExpiryDate> {
        val validation = when (validationResult) {
            CardExpiryDateValidationResult.VALID -> Validation.Valid

            CardExpiryDateValidationResult.INVALID_TOO_FAR_IN_THE_FUTURE ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_far_in_future)

            CardExpiryDateValidationResult.INVALID_TOO_OLD ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid_too_old)

            CardExpiryDateValidationResult.INVALID_DATE_FORMAT ->
                Validation.Invalid(R.string.checkout_expiry_date_not_valid)

            CardExpiryDateValidationResult.INVALID_OTHER_REASON -> if (fieldPolicy?.isRequired() == false) {
                Validation.Valid
            } else {
                Validation.Invalid(R.string.checkout_expiry_date_not_valid)
            }
        }
        return FieldState(expiryDate, validation)
    }

    fun mapSecurityCodeValidation(
        securityCode: String,
        cvcUIState: InputFieldUIState,
        validationResult: CardSecurityCodeValidationResult
    ): FieldState<String> {
        val normalizedSecurityCode = StringUtil.normalize(securityCode)
        val length = normalizedSecurityCode.length
        val invalidState = Validation.Invalid(R.string.checkout_security_code_not_valid)

        val validation = when {
            cvcUIState == InputFieldUIState.HIDDEN -> Validation.Valid
            cvcUIState == InputFieldUIState.OPTIONAL && length == 0 -> Validation.Valid
            else -> {
                when (validationResult) {
                    CardSecurityCodeValidationResult.INVALID -> invalidState
                    CardSecurityCodeValidationResult.VALID -> Validation.Valid
                }
            }
        }

        return FieldState(normalizedSecurityCode, validation)
    }
}
