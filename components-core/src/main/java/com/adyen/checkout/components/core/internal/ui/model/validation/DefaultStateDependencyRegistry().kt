/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.validation

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultStateDependencyRegistry<FI, S>: StateDependencyRegistry<FI, S> {
    override fun getDependencies(fieldId: FI): List<(S) -> Any?>? = null
}
