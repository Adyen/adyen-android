/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.Validation
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock

internal class MBWayValidatorRegistryTest {

    private lateinit var validatorRegistry: MBWayValidatorRegistry

    private var state: MBWayComponentState = mock()

    @BeforeEach
    fun setup() {
        validatorRegistry = MBWayValidatorRegistry()
    }

    @Test
    fun `when validate is called for country code, then validation should be valid`() {
        val result =
            validatorRegistry.validate(state, MBWayFieldId.COUNTRY_CODE, "")
        assertTrue(result is Validation.Valid)
    }

    @Test
    fun `when validate is called for invalid phone number, then validation should be invalid`() {
        val invalidPhoneNumber = ""
        val result = validatorRegistry.validate(
            state,
            MBWayFieldId.PHONE_NUMBER,
            invalidPhoneNumber,
        )

        assertTrue(result is Validation.Invalid)
    }

    @Test
    fun `when validate is called for valid phone number, then validation should be invalid`() {
        val validPhoneNumber = "123456789"
        val result = validatorRegistry.validate(
            state,
            MBWayFieldId.PHONE_NUMBER,
            validPhoneNumber,
        )

        assertTrue(result is Validation.Valid)
    }
}
