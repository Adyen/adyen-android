/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/2/2025.
 */

package com.adyen.checkout.payto.internal.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments.arguments
import org.junit.jupiter.params.provider.MethodSource

internal class PayToValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("getPhoneNumberTestCases")
    fun `when phone number is tested, then return expected result`(phoneNumber: String, expected: Boolean) {
        assertEquals(expected, PayToValidationUtils.isPhoneNumberValid(phoneNumber))
    }

    @ParameterizedTest
    @MethodSource("getAbnNumberTestCases")
    fun `when ABN number is tested, then return expected result`(abnNumber: String, expected: Boolean) {
        assertEquals(expected, PayToValidationUtils.isAbnNumberValid(abnNumber))
    }

    @ParameterizedTest
    @MethodSource("getOrganizationIdTestCases")
    fun `when organization ID is tested, then return expected result`(organizationId: String, expected: Boolean) {
        assertEquals(expected, PayToValidationUtils.isOrganizationIdValid(organizationId))
    }

    @ParameterizedTest
    @MethodSource("getBsbAccountNumberTestCases")
    fun `when BSB account number is tested, then return expected result`(bsbAccountNumber: String, expected: Boolean) {
        assertEquals(expected, PayToValidationUtils.isBsbAccountNumberValid(bsbAccountNumber))
    }

    @ParameterizedTest
    @MethodSource("getBsbStateBranchTestCases")
    fun `when BSB state branch is tested, then return expected result`(bsbStateBranch: String, expected: Boolean) {
        assertEquals(expected, PayToValidationUtils.isBsbStateBranchValid(bsbStateBranch))
    }

    companion object {

        @JvmStatic
        fun getPhoneNumberTestCases() = listOf(
            // phoneNumber, expectedValidationResult
            arguments("+1-123456789", true),
            arguments("+44-789456123", true),
            arguments("+91-9876543210", true),
            arguments("+33-145678912", true),
            arguments("+49-1523456789", true),
            arguments("+61-412345678", true),
            arguments("+81-9076543210", true),
            arguments("123456789", false),
            arguments("+1-", false),
            arguments("+1-abc", false),
            arguments("++1-123456789", false),
            arguments("+123-000000000000000000000000000000", false),
            arguments("+1-00000000000000000000000000000", false),
        )

        @JvmStatic
        fun getAbnNumberTestCases() = listOf(
            // abnNumber, expectedValidationResult
            arguments("123456789", true),
            arguments("12345678901", true),
            arguments("00000000000", true),
            arguments("99999999999", true),
            arguments("9876543210", false),
            arguments("abcdefghijk", false),
            arguments("12345", false),
        )

        @JvmStatic
        fun getOrganizationIdTestCases() = listOf(
            // organizationId, expectedValidationResult
            arguments("companysearch83n293c8", true),
            arguments("12345678901", true),
            arguments("123456789", true),
            arguments("Org_#Valid", false),
            arguments("Invalid Org", false),
            arguments("Org#Invalid", false),
        )

        @JvmStatic
        fun getBsbAccountNumberTestCases() = listOf(
            // bsbAccountNumber, expectedValidationResult
            arguments("123456", true),
            arguments("987654", true),
            arguments("000000", true),
            arguments("654321", true),
            arguments("555555", true),
            arguments("12345", false),
            arguments("abcdef", false),
        )

        @JvmStatic
        fun getBsbStateBranchTestCases() = listOf(
            // bsbStateBranch, expectedValidationResult
            arguments("Main Branch", true),
            arguments("Branch-123", true),
            arguments("1234", true),
            arguments("Central Branch", true),
            arguments("Northside Bank", true),
            arguments("", false),
            arguments("This is a very long branch name that exceeds 28 characters", false),
        )
    }
}
