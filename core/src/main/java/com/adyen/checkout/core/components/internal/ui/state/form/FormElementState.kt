/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 1/9/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import androidx.annotation.RestrictTo
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * The form-level state of one visible UI element.
 *
 * @param isValid Elements that cannot be invalid, such as switches, use true.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormElementState<Id : FormElementId>(
    val id: Id,
    val isValid: Boolean,
)

/**
 * Returns this text input as a form element, or null when it is hidden.
 */
// TODO - Form fields phase 3: Card will use this helper when it starts building its form state.
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> TextInputComponentState.toFormElementIfVisible(id: Id): FormElementState<Id>? {
    val isVisible = requirementPolicy != RequirementPolicy.Hidden
    return if (isVisible) {
        val isValid = error == null
        FormElementState(
            id = id,
            isValid = isValid,
        )
    } else {
        null
    }
}
