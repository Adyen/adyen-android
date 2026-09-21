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
 * Which elements a form shows, in the order the shopper sees them.
 *
 * The order lives here rather than falling out of the layout because Compose fixes a text input's keyboard action when
 * the field is created and offers no way to ask whether another focusable field follows it.
 *
 * Only visible items are added to the list.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormState<Id : FormElementId>(
    val elements: List<FormElementState<Id>>,
)
