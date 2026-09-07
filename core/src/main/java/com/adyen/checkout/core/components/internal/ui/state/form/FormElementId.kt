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
 * Identifies a single element of a form. Every component declares its own set, usually as an enum.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FormElementId {

    /**
     * Whether this element is a text input. Return false for switches and pickers: they keep their place in the order,
     * but the keyboard and focus rules skip them.
     */
    val isTextInput: Boolean
}
