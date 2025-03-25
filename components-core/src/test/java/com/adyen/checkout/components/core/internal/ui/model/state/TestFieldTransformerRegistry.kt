/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.state

import com.adyen.checkout.components.core.internal.ui.model.transformer.FieldTransformerRegistry
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

internal class TestFieldTransformerRegistry : FieldTransformerRegistry<TestFieldId> {
    private val transformedFields = mutableSetOf<TestFieldId>()

    override fun <T> transform(key: TestFieldId, value: T): T {
        transformedFields.add(key)
        return value
    }

    fun assertIsTransformed(expected: TestFieldId) {
        assertTrue(transformedFields.contains(expected))
    }

    fun assertNotTransformed(expected: TestFieldId) {
        assertFalse(transformedFields.contains(expected))
    }
}
