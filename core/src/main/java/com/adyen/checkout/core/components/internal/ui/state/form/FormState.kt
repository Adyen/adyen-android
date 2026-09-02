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
 * The structure of a form: which elements it currently shows, in which order, and what the form's own rules need to
 * know about each of them.
 *
 * Compose decides the keyboard action of a text input when the field is created, and offers no way to ask whether
 * another focusable field follows. The order therefore has to live here, in the state layer, rather than fall out of
 * the layout.
 *
 * This holds structure only. A pending [FocusRequest] is not structure but a move waiting to happen, and the component
 * state already owns one, so it is read from there rather than copied in here.
 *
 * @param elements The elements the form currently shows, in the order the shopper sees them. Being in the list means
 * the element is visible, and the position in the list is the position on screen. Visibility is left out of
 * [FormElementState] because every rule here wants the visible elements, so filtering once when the form is built
 * cannot be forgotten by a caller.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class FormState<Id : FormElementId>(
    val elements: List<FormElementState<Id>>,
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
data class FocusRequest<Id : FormElementId>(
    val id: Id,
    val keepErrorHighlight: Boolean = false,
)

/**
 * This request unless [id] has just taken the focus it asked for, in which case there is nothing left pending.
 *
 * An answered request has to be dropped, or it would outlive the move it asked for and make the shopper's next tap on
 * the same field look programmatic.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
fun <Id : FormElementId> FocusRequest<Id>?.remainingAfter(id: Id, hasFocus: Boolean): FocusRequest<Id>? =
    this?.takeUnless { hasFocus && it.id == id }
