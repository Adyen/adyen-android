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
 * One element of a form, holding what the form's own rules need to know about it rather than the element's state. A
 * component owns its fields and is the only thing that can change them, so copying one in here would put the same value
 * behind two routes.
 *
 * Add a fact when a rule in this package needs it and only the component can answer it. Each one costs a line in every
 * component's builder and can drift from what it was derived from.
 *
 * @param isValid Elements that cannot be invalid, such as a switch, pass true.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormElementState<Id : FormElementId>(
    val id: Id,
    val isValid: Boolean,
)

/**
 * This field as the form sees it, or null when it is not visible on screen.
 * The only source of truth for visibility and validity of a field exists here.
 */
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
