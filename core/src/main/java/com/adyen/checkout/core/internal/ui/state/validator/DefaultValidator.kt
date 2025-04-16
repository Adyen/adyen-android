/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 4/2/2025.
 */

package com.adyen.checkout.core.internal.ui.state.validator

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.internal.ui.state.DelegateState
import com.adyen.checkout.core.internal.ui.state.model.Validation

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultValidator : FieldValidator<DelegateState, Any> {
    override fun validate(state: DelegateState, input: Any) = Validation.Valid
}
