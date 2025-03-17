/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.validation

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.Validation

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FieldValidatorRegistry<K, S> {
    fun <T> validate(key: K, value: T, state: S): Validation
}
