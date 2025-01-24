/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/1/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.transformer

import androidx.annotation.RestrictTo

/**
 * A [FieldTransformerRegistry] is being used to implement value transformation for fields. This is useful when the
 * value should get transformed before validation is performed or before it is being processed further.
 * State should hold the original value, while transformed value will be used for other operations.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FieldTransformerRegistry<K> {
    fun <T> transform(key: K, value: T): T
}
