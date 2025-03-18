/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.ui.model.validation.FieldValidatorRegistry
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class TestFieldValidatorRegistry : FieldValidatorRegistry<TestFieldId, TestDelegateState> {
    private val validatedFields = mutableSetOf<TestFieldId>()

    override fun <T> validate(key: TestFieldId, value: T, state: TestDelegateState): Validation {
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
