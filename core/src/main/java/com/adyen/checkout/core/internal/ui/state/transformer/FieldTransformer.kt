/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 24/1/2025.
 */

package com.adyen.checkout.core.internal.ui.state.transformer

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FieldTransformer<T> {
    fun transform(value: T): T
}
