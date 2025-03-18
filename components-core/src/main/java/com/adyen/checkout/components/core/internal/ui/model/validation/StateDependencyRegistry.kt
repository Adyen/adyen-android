/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 17/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.validation

import androidx.annotation.RestrictTo

/**
 * A [StateDependencyRegistry] is being used to implement value relationship between fields. This is useful when
 * an update to a specific field in the state triggers an update or a validation in a different field.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface StateDependencyRegistry<FI, S> {
    fun getDependencies(fieldId: FI): List<(S) -> Any?>?
}
