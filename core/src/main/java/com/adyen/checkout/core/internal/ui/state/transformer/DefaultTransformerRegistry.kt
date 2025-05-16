/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/3/2025.
 */

package com.adyen.checkout.core.internal.ui.state.transformer

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.ui.state.model.FieldId

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultTransformerRegistry<FI : FieldId> : FieldTransformerRegistry<FI> {
    override fun <T> transform(fieldId: FI, value: T): T = value
}
