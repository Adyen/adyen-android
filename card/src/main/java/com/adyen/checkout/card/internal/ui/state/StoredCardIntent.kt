/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 23/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface StoredCardIntent : ComponentStateIntent {

    // User input intents
    data class UpdateSecurityCode(val securityCode: String) : StoredCardIntent

    /**
     * A field gained or lost focus. Keyed on the field rather than named after it, because focus is plumbing with no
     * meaning of its own — unlike a value change, which carries the field's own rules.
     */
    data class UpdateFieldFocus(val id: StoredCardFormElementId, val hasFocus: Boolean) : StoredCardIntent

    /**
     * The UI has acted on a focus request. Only needed for a request it could not fulfil, since a request that results
     * in a focus gain is cleared by that gain instead.
     */
    data class FocusRequestConsumed(val id: StoredCardFormElementId) : StoredCardIntent

    // System intents
    data class UpdateLoading(val isLoading: Boolean) : StoredCardIntent

    data object HighlightValidationErrors : StoredCardIntent
}
