/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.transformer

import androidx.annotation.RestrictTo

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultTransformerRegistry<FI> : FieldTransformerRegistry<FI> {
    override fun <T> transform(key: FI, value: T): T = value
}
