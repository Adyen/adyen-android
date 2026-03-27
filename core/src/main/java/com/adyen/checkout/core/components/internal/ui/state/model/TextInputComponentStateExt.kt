/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 27/3/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.model

import androidx.annotation.RestrictTo

/**
 * Returns the raw string value of a text input based on its requirement policy. This value is usually set inside the
 * `PaymentMethodDetails` class and sent as it is through the `onSubmit` callback.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.getPaymentDataValue(): String? {
    return text.takeIf { requirementPolicy != RequirementPolicy.Hidden && it.isNotBlank() }
}

/**
 * Returns true if the text field is hidden or optional and empty -> field is automatically valid
 * Returns false if the text field is required or optional and not empty -> further validation is needed
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun TextInputComponentState.isNotRequiredAndValid(): Boolean {
    return when (requirementPolicy) {
        RequirementPolicy.Hidden -> true
        RequirementPolicy.Optional -> text.isBlank()
        RequirementPolicy.Required -> false
    }
}
