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
 * Identifies any UI element that belongs to a form, not only text inputs.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface FormElementId {
    val isTextInput: Boolean
}
