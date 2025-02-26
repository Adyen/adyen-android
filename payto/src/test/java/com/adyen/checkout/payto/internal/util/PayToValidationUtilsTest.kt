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
import org.junit.jupiter.params.provider.Arguments
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
            Arguments.of("+1-123456789", true),
            Arguments.of("+44-789456123", true),
            Arguments.of("+91-9876543210", true),
            Arguments.of("+33-145678912", true),
            Arguments.of("+49-1523456789", true),
            Arguments.of("+61-412345678", true),
            Arguments.of("+81-9076543210", true),
            Arguments.of("123456789", false),
            Arguments.of("+1-", false),
            Arguments.of("+1-abc", false),
            Arguments.of("++1-123456789", false),
            Arguments.of("+123-000000000000000000000000000000", false),
            Arguments.of("+1-00000000000000000000000000000", false),
        )

        @JvmStatic
        fun getAbnNumberTestCases() = listOf(
            // abnNumber, expectedValidationResult
            Arguments.of("123456789", true),
            Arguments.of("12345678901", true),
            Arguments.of("00000000000", true),
            Arguments.of("99999999999", true),
            Arguments.of("9876543210", false),
            Arguments.of("abcdefghijk", false),
            Arguments.of("12345", false),
        )

        @JvmStatic
        fun getOrganizationIdTestCases() = listOf(
            // organizationId, expectedValidationResult
            Arguments.of("companysearch83n293c8", true),
            Arguments.of("12345678901", true),
            Arguments.of("123456789", true),
            Arguments.of("Org_#Valid", false),
            Arguments.of("Invalid Org", false),
            Arguments.of("Org#Invalid", false),
        )

        @JvmStatic
        fun getBsbAccountNumberTestCases() = listOf(
            // bsbAccountNumber, expectedValidationResult
            Arguments.of("123456", true),
            Arguments.of("987654", true),
            Arguments.of("000000", true),
            Arguments.of("654321", true),
            Arguments.of("555555", true),
            Arguments.of("12345", false),
            Arguments.of("abcdef", false),
        )

        @JvmStatic
        fun getBsbStateBranchTestCases() = listOf(
            // bsbStateBranch, expectedValidationResult
            Arguments.of("Main Branch", true),
            Arguments.of("Branch-123", true),
            Arguments.of("1234", true),
            Arguments.of("Central Branch", true),
            Arguments.of("Northside Bank", true),
            Arguments.of("", false),
            Arguments.of("This is a very long branch name that exceeds 28 characters", false),
        )
    }
}
