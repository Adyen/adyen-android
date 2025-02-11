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

internal class PayToOutputData(
    mobilePhoneNumber: String,
    emailAddress: String,
    abnNumber: String,
    organizationId: String,
    bsbAccountNumber: String,
    bsbStateBranch: String,
    firstName: String,
    lastName: String,
) : OutputData {

    val phoneNumberFieldState: FieldState<String> = validateMobileNumber(mobilePhoneNumber)
    val emailAddressFieldState: FieldState<String> = validateEmailAddress(emailAddress)
    val abnNumberFieldState: FieldState<String> = validateAbnNumber(abnNumber)
    val organizationIdFieldState: FieldState<String> = validateOrganizationId(organizationId)
    val bsbAccountNumberFieldState: FieldState<String> = validateBsbAccountNumber(bsbAccountNumber)
    val bsbStateBranchFieldState: FieldState<String> = validateBsbStateBranch(bsbStateBranch)
    val firstNameFieldState: FieldState<String> = validateFirstName(firstName)
    val lastNameFieldState: FieldState<String> = validateLastName(lastName)

    override val isValid: Boolean = true

    private fun validateMobileNumber(mobileNumber: String): FieldState<String> = FieldState(
        mobileNumber,
        Validation.Valid
    )

    private fun validateEmailAddress(emailAddress: String): FieldState<String> = FieldState(
        emailAddress,
        Validation.Valid
    )

    private fun validateAbnNumber(abnNumber: String): FieldState<String> = FieldState(
        abnNumber,
        Validation.Valid
    )

    private fun validateOrganizationId(organizationId: String): FieldState<String> = FieldState(
        organizationId,
        Validation.Valid
    )

    private fun validateBsbAccountNumber(bsbAccountNumber: String): FieldState<String> = FieldState(
        bsbAccountNumber,
        Validation.Valid
    )

    private fun validateBsbStateBranch(bsbStateBranch: String): FieldState<String> = FieldState(
        bsbStateBranch,
        Validation.Valid
    )

    private fun validateFirstName(firstName: String): FieldState<String> = FieldState(
        firstName,
        Validation.Valid
    )

    private fun validateLastName(lastName: String): FieldState<String> = FieldState(
        lastName,
        Validation.Valid
    )
}
