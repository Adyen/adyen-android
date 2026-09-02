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
 * A request for the UI to move focus to a field.
 *
 * @param keepErrorHighlight Whether the field keeps an error it is already showing. Only focus that follows pay uses
 * true, because the point of that move is to show the error. Everything else behaves as if the shopper had tapped.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FocusRequest<Id : FormElementId>(
    val id: Id,
    val keepErrorHighlight: Boolean = false,
)
