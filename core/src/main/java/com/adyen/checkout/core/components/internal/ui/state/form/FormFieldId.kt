/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 17/8/2026.
 */

package com.adyen.checkout.core.components.internal.ui.state.form

import androidx.annotation.RestrictTo

/**
 * Identifies a single element of a form. Every component that has a form declares its own set of ids, usually as an
 * enum.
 *
 * Elements that are not text inputs, such as a switch or a picker, are also form fields: they take part in the
 * ordering, because the UI renders the form by walking [FormState.order].
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FormFieldId {

    /**
     * Whether this field is a text input.
     *
     * This governs the derivation of the keyboard action only, since only a text input has a keyboard. It has no
     * effect on ordering or on focus.
     */
    val isTextInput: Boolean
}
