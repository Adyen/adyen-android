/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 5/2/2025.
 */

package com.adyen.checkout.core.internal.ui.state.transformer

import androidx.annotation.RestrictTo

// TODO Remove unused suppression
@Suppress("unused")
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultTransformer : FieldTransformer<Any> {
    override fun transform(value: Any) = value
}
