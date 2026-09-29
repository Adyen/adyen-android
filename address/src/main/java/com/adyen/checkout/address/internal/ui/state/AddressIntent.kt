/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface AddressIntent : ComponentStateIntent {

    data class UpdateCountry(val country: CountryModel) : AddressIntent

    data class UpdatePostalCode(val postalCode: String) : AddressIntent

    data class UpdateFieldFocus(val id: AddressFormElementId, val hasFocus: Boolean) : AddressIntent

    data class FocusRequestConsumed(val id: AddressFormElementId) : AddressIntent

    data object HighlightValidationErrors : AddressIntent

    /** Replaces the whole form with [address], or resets it when null, dropping edits that were never confirmed. */
    data class Prefill(val address: AddressModel?) : AddressIntent
}
