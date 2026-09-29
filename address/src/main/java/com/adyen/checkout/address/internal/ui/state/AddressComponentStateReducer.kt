/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.state.ComponentStateReducer

internal class AddressComponentStateReducer(
    private val componentStateFactory: AddressComponentStateFactory,
) : ComponentStateReducer<AddressComponentState, AddressIntent> {

    override fun reduce(state: AddressComponentState, intent: AddressIntent): AddressComponentState {
        return when (intent) {
            is AddressIntent.UpdateCountry -> state.copy(
                country = state.country.copy(selectedCountry = intent.country),
            )

            is AddressIntent.UpdatePostalCode -> state.copy(
                postalCode = state.postalCode.updateText(intent.postalCode),
            )

            is AddressIntent.Prefill -> componentStateFactory.createState(intent.address)

            is AddressIntent.UpdateFieldFocus,
            is AddressIntent.FocusRequestConsumed,
            is AddressIntent.HighlightValidationErrors -> state
        }
    }
}
