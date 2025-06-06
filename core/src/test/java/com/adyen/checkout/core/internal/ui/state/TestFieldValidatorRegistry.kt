/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.core.internal.ui.state

import com.adyen.checkout.core.internal.ui.state.model.Validation
import com.adyen.checkout.core.internal.ui.state.validator.FieldValidatorRegistry
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class TestFieldValidatorRegistry : FieldValidatorRegistry<TestDelegateState, TestFieldId> {
    private val validatedFields = mutableSetOf<TestFieldId>()

    override fun <T> validate(state: TestDelegateState, key: TestFieldId, value: T): Validation {
        validatedFields.add(key)
        return Validation.Valid
    }

    fun assertIsValidated(expected: TestFieldId) {
        assertTrue(validatedFields.contains(expected))
    }

    fun assertNotValidated(expected: TestFieldId) {
        assertFalse(validatedFields.contains(expected))
    }
}
