/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.components.core.internal.ui.model.validation

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.Validation

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultValidator : FieldValidator<Any, Any> {
    override fun validate(input: Any, state: Any) = Validation.Valid
}
