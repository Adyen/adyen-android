/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/2/2025.
 */

package com.adyen.checkout.payto.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.payto.R
import com.adyen.checkout.payto.internal.util.PayToValidationUtils

@Suppress("LongParameterList")
internal class PayToOutputData(
    val mode: PayToMode,
    val payIdTypeModel: PayIdTypeModel?,
    val mobilePhoneNumber: String,
    val emailAddress: String,
    val abnNumber: String,
    val organizationId: String,
    val bsbAccountNumber: String,
    val bsbStateBranch: String,
    val firstName: String,
    val lastName: String,
) : OutputData {

    val phoneNumberFieldState: FieldState<String> = validateMobileNumber(mobilePhoneNumber)
    val emailAddressFieldState: FieldState<String> = validateEmailAddress(emailAddress)
    val abnNumberFieldState: FieldState<String> = validateAbnNumber(abnNumber)
    val organizationIdFieldState: FieldState<String> = validateOrganizationId(organizationId)
    val bsbAccountNumberFieldState: FieldState<String> = validateBsbAccountNumber(bsbAccountNumber)
    val bsbStateBranchFieldState: FieldState<String> = validateBsbStateBranch(bsbStateBranch)
    val firstNameFieldState: FieldState<String> = validateFirstName(firstName)
    val lastNameFieldState: FieldState<String> = validateLastName(lastName)

    override val isValid: Boolean =
        phoneNumberFieldState.validation.isValid() &&
            emailAddressFieldState.validation.isValid() &&
            abnNumberFieldState.validation.isValid() &&
            organizationIdFieldState.validation.isValid() &&
            bsbAccountNumberFieldState.validation.isValid() &&
            bsbStateBranchFieldState.validation.isValid() &&
            firstNameFieldState.validation.isValid() &&
            lastNameFieldState.validation.isValid()

    private fun validateMobileNumber(phoneNumber: String): FieldState<String> =
        if (phoneNumber.isNotBlank() && PayToValidationUtils.isPhoneNumberValid(phoneNumber)) {
            FieldState(phoneNumber, Validation.Valid)
        } else {
            FieldState(
                phoneNumber,
                Validation.Invalid(R.string.checkout_payto_payid_phone_number_invalid),
            )
        }

    private fun validateEmailAddress(emailAddress: String): FieldState<String> =
        if (emailAddress.isNotBlank() && ValidationUtils.isEmailValid(emailAddress)) {
            FieldState(emailAddress, Validation.Valid)
        } else {
            FieldState(
                emailAddress,
                Validation.Invalid(R.string.checkout_payto_payid_email_address_invalid),
            )
        }

    private fun validateAbnNumber(abnNumber: String): FieldState<String> =
        if (abnNumber.isNotBlank() && PayToValidationUtils.isAbnNumberValid(abnNumber)) {
            FieldState(abnNumber, Validation.Valid)
        } else {
            FieldState(
                abnNumber,
                Validation.Invalid(R.string.checkout_payto_payid_abn_number_invalid),
            )
        }

    private fun validateOrganizationId(organizationId: String): FieldState<String> =
        if (organizationId.isNotBlank() && PayToValidationUtils.isOrganizationIdValid(organizationId)) {
            FieldState(organizationId, Validation.Valid)
        } else {
            FieldState(
                organizationId,
                Validation.Invalid(R.string.checkout_payto_payid_organization_id_invalid),
            )
        }

    private fun validateBsbAccountNumber(bsbAccountNumber: String): FieldState<String> =
        if (bsbAccountNumber.isNotBlank() && PayToValidationUtils.isBsbAccountNumberValid(bsbAccountNumber)) {
            FieldState(bsbAccountNumber, Validation.Valid)
        } else {
            FieldState(
                bsbAccountNumber,
                Validation.Invalid(R.string.checkout_payto_bsb_account_number_invalid),
            )
        }

    private fun validateBsbStateBranch(bsbStateBranch: String): FieldState<String> =
        if (bsbStateBranch.isNotBlank() && PayToValidationUtils.isBsbStateBranchValid(bsbStateBranch)) {
            FieldState(bsbStateBranch, Validation.Valid)
        } else {
            FieldState(
                bsbStateBranch,
                Validation.Invalid(R.string.checkout_payto_bsb_state_branch_invalid),
            )
        }

    private fun validateFirstName(firstName: String): FieldState<String> = if (firstName.isNotBlank()) {
        FieldState(firstName, Validation.Valid)
    } else {
        FieldState(
            firstName,
            Validation.Invalid(R.string.checkout_payto_first_name_invalid),
        )
    }

    private fun validateLastName(lastName: String): FieldState<String> = if (lastName.isNotBlank()) {
        FieldState(lastName, Validation.Valid)
    } else {
        FieldState(
            lastName,
            Validation.Invalid(R.string.checkout_payto_last_name_invalid),
        )
    }
}
