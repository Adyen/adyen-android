/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface MBWayIntent : ComponentStateIntent {

    data class UpdateCountry(val country: CountryModel) : MBWayIntent

    data class UpdateLoading(val isLoading: Boolean) : MBWayIntent

    data class UpdatePhoneNumber(val number: String) : MBWayIntent

    /**
     * A field gained or lost focus. Keyed on the field rather than named after it, because focus is plumbing with no
     * meaning of its own — unlike a value change, which carries the field's own rules.
     */
    data class UpdateFieldFocus(val id: MBWayFieldId, val hasFocus: Boolean) : MBWayIntent

    /**
     * The UI has acted on a focus request. Only needed for a request it could not fulfil, since a request that results
     * in a focus gain is cleared by that gain instead.
     */
    data class FocusRequestConsumed(val id: MBWayFieldId) : MBWayIntent

    data object HighlightValidationErrors : MBWayIntent
}
