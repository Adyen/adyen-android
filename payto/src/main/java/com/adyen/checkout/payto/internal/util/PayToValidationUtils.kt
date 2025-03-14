/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/2/2025.
 */

package com.adyen.checkout.payto.internal.util

import java.util.regex.Pattern

internal object PayToValidationUtils {

    private const val PHONE_NUMBER_REGEX = "^\\+[0-9]{1,3}-[1-9]{1,1}[0-9]{1,29}\$"
    private val PHONE_NUMBER_PATTERN = Pattern.compile(PHONE_NUMBER_REGEX)

    private const val ABN_NUMBER_REGEX = "^((\\d{9})|(\\d{11}))\$"
    private val ABN_NUMBER_PATTERN = Pattern.compile(ABN_NUMBER_REGEX)

    private const val ORGANIZATION_ID_REGEX = "^[!-@\\[-~][ -@\\[-~]{0,254}[!-@\\[-~]\$"
    private val ORGANIZATION_ID_PATTERN = Pattern.compile(ORGANIZATION_ID_REGEX)

    private const val BSB_STATE_BRANCH_REGEX = "^\\d{6}\$"
    private val BSB_STATE_BRANCH_PATTERN = Pattern.compile(BSB_STATE_BRANCH_REGEX)

    private const val BSB_ACCOUNT_NUMBER_REGEX = "^[ -~]{1,28}\$"
    private val BSB_ACCOUNT_NUMBER_PATTERN = Pattern.compile(BSB_ACCOUNT_NUMBER_REGEX)

    fun isPhoneNumberValid(phoneNumber: String): Boolean {
        return PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches()
    }

    fun isAbnNumberValid(abnNumber: String): Boolean {
        return ABN_NUMBER_PATTERN.matcher(abnNumber).matches()
    }

    fun isOrganizationIdValid(organizationId: String): Boolean {
        return ORGANIZATION_ID_PATTERN.matcher(organizationId).matches()
    }

    fun isBsbStateBranchValid(bsbStateBranch: String): Boolean {
        return BSB_STATE_BRANCH_PATTERN.matcher(bsbStateBranch).matches()
    }

    fun isBsbAccountNumberValid(bsbAccountNumber: String): Boolean {
        return BSB_ACCOUNT_NUMBER_PATTERN.matcher(bsbAccountNumber).matches()
    }
}
