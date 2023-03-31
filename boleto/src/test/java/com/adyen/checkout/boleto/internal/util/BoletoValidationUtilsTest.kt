/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 4/4/2023.
 */

package com.adyen.checkout.boleto.internal.util

import com.adyen.checkout.boleto.R
import com.adyen.checkout.components.core.internal.ui.model.Validation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

internal class BoletoValidationUtilsTest {

    @ParameterizedTest
    @MethodSource("firstNameSource")
    fun `first name value is set, then validation should match expected validation`(
        firstName: String,
        expectedValidation: Validation
    ) {
        assertEquals(expectedValidation, BoletoValidationUtils.validateFirstName(firstName).validation)
    }

    @ParameterizedTest
    @MethodSource("lastNameSource")
    fun `last name value is set, then validation should match expected validation`(
        lastName: String,
        expectedValidation: Validation
    ) {
        assertEquals(expectedValidation, BoletoValidationUtils.validateLastName(lastName).validation)
    }

    @ParameterizedTest
    @MethodSource("emailSource")
    fun `email and isEmailEnabled value is set, then actual validation should match expected validation`(
        isEmailEnabled: Boolean,
        email: String,
        expectedValidation: Validation
    ) {
        assertEquals(expectedValidation, BoletoValidationUtils.validateShopperEmail(isEmailEnabled, email).validation)
    }

    companion object {
        @JvmStatic
        fun firstNameSource() = listOf(
            // firstName, expected validation
            Arguments.arguments("firstname", Validation.Valid),
            Arguments.arguments("", Validation.Invalid(reason = R.string.checkout_boleto_first_name_invalid)),
        )

        @JvmStatic
        fun lastNameSource() = listOf(
            // lastName, expected validation
            Arguments.arguments("firstname", Validation.Valid),
            Arguments.arguments("", Validation.Invalid(reason = R.string.checkout_boleto_last_name_invalid)),
        )

        @JvmStatic
        fun emailSource() = listOf(
            // isEmailEnabled, email, expected validation
            Arguments.arguments(false, "email", Validation.Valid),
            Arguments.arguments(false, "email@tezt.com", Validation.Valid),
            Arguments.arguments(true, "", Validation.Invalid(reason = R.string.checkout_boleto_email_invalid)),
            Arguments.arguments(true, "email", Validation.Invalid(reason = R.string.checkout_boleto_email_invalid)),
            Arguments.arguments(true, "email@test.com", Validation.Valid)
        )
    }
}
