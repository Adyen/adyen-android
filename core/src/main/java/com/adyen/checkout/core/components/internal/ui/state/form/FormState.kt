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
 * The structure of a form: which fields it currently shows, and in which order.
 *
 * Compose decides the keyboard action of a text input when the field is created, and offers no way to ask whether
 * another focusable field follows. The order therefore has to live here, in the state layer, rather than fall out of
 * the layout.
 *
 * @param order The fields the form currently shows, in the order the shopper sees them. Being in the list means the
 * field is visible, and the position in the list is the position on screen.
 * @param focusRequest A focus move the state layer is asking the UI to make, or null if there is none pending. The UI
 * reports back once it has acted on it, which clears it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormState<Id : FormFieldId>(
    val order: List<Id>,
    val focusRequest: FocusRequest<Id>? = null,
)

/**
 * A request for the UI to move focus to a field.
 *
 * @param id The field that should receive focus.
 * @param keepErrorHighlight Whether the field keeps an error it is already showing while it receives focus. Focus that
 * follows the shopper pressing pay uses true, because the point of that move is to show the error. Every other
 * request, such as focusing the field after a prefilled one, uses false so that the field behaves as if the shopper
 * had tapped it.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FocusRequest<Id : FormFieldId>(
    val id: Id,
    val keepErrorHighlight: Boolean = false,
)
