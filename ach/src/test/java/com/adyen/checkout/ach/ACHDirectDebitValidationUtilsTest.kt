/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by onurk on 31/1/2023.
 */

package com.adyen.checkout.ach

import com.adyen.checkout.components.ui.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class ACHDirectDebitValidationUtilsTest {

    @Test
    fun `if an account number length is lower than minumum length,return Invalid`() {
        val accountNumber = "123"
        val validation = ACHDirectDebitValidationUtils.validateBankAccountNumber(accountNumber).validation

        val expected = getInvalidAccountNumberValidation()

        assertEquals(expected, validation)
    }

    @Test
    fun `if an account number length is higher than maximum length, return Invalid`() {
        val accountNumber = "123456789012345678"
        val validation = ACHDirectDebitValidationUtils.validateBankAccountNumber(accountNumber).validation

        val expected = getInvalidAccountNumberValidation()

        assertEquals(expected, validation)
    }

    @Test
    fun `if an account number is between minimum and maximum account number, return Valid`() {
        val accountNumber = "123456789012"
        val validation = ACHDirectDebitValidationUtils.validateBankAccountNumber(accountNumber).validation

        assertEquals(Validation.Valid, validation)
    }

    @Test
    fun `if bank location id is not equal to bank location length, return Invalid `() {
        val bankLocationId = "123"
        val validation = ACHDirectDebitValidationUtils.validateBankLocationId(bankLocationId).validation

        val expected = Validation.Invalid(reason = R.string.checkout_ach_bank_account_location_invalid)

        assertEquals(expected, validation)
    }

    @Test
    fun `if bank location id is  equal to bank location length, return Valid `() {
        val bankLocationId = "123456789"
        val validation = ACHDirectDebitValidationUtils.validateBankLocationId(bankLocationId).validation

        assertEquals(Validation.Valid, validation)
    }

    @Test
    fun `if owner name is empty, return Invalid`() {
        val ownerName = ""
        val validation = ACHDirectDebitValidationUtils.validateOwnerName(ownerName).validation

        val expected = Validation.Invalid(reason = R.string.checkout_ach_bank_account_holder_name_invalid)

        assertEquals(expected, validation)
    }

    @Test
    fun `if owner name is not empty, return Valid`() {
        val ownerName = "Joseph"
        val validation = ACHDirectDebitValidationUtils.validateOwnerName(ownerName).validation

        assertEquals(Validation.Valid, validation)
    }

    private fun getInvalidAccountNumberValidation() = Validation.Invalid(
        reason = R.string.checkout_ach_bank_account_number_invalid
    )
}
