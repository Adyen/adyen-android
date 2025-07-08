/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 6/3/2025.
 */

package com.adyen.checkout.test.ui.state

import com.adyen.checkout.core.components.internal.ui.state.model.FieldId
import com.adyen.checkout.core.components.internal.ui.state.transformer.FieldTransformerRegistry
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class TestFieldTransformerRegistry<FI : FieldId> : FieldTransformerRegistry<FI> {

    private val transformations = mutableMapOf<Pair<FI, Any?>, Any?>()

    private val transformedFields = mutableSetOf<FI>()

    override fun <T> transform(key: FI, value: T): T {
        transformedFields.add(key)
        @Suppress("UNCHECKED_CAST")
        return transformations[key to value] as? T ?: value
    }

    fun <T> setTransformation(key: FI, originalValue: T, transformedValue: T) {
        transformations[key to originalValue] = transformedValue
    }

    fun assertIsTransformed(expected: FI) {
        assertTrue(transformedFields.contains(expected))
    }

    fun assertNotTransformed(expected: FI) {
        assertFalse(transformedFields.contains(expected))
    }
}
