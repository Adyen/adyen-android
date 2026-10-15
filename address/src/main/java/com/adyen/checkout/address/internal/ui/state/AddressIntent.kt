/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.data.model.AddressItem
import com.adyen.checkout.address.internal.ui.model.AddressModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateIntent

internal sealed interface AddressIntent : ComponentStateIntent {

    /** @param countryCode The ISO 3166-1 alpha-2 code of the picked country. */
    data class UpdateCountry(val countryCode: String) : AddressIntent

    /** @param stateCode The ISO 3166-2 code of the picked state. */
    data class UpdateStateOrProvince(val stateCode: String) : AddressIntent

    data class UpdateStreet(val street: String) : AddressIntent

    data class UpdateHouseNumberOrName(val houseNumberOrName: String) : AddressIntent

    data class UpdatePostalCode(val postalCode: String) : AddressIntent

    data class UpdateCity(val city: String) : AddressIntent

    data class UpdateFieldFocus(val id: AddressFormElementId, val hasFocus: Boolean) : AddressIntent

    data class FocusRequestConsumed(val id: AddressFormElementId) : AddressIntent

    data object HighlightValidationErrors : AddressIntent

    /**
     * Resets the form to [address], or empties it when null, dropping edits that were never confirmed. Dispatched every
     * time the address component is shown.
     */
    data class Reset(val address: AddressModel?) : AddressIntent

    data class CountriesLoaded(val result: Result<List<AddressItem>>) : AddressIntent

    /** @param countryCode The country the states were fetched for, so a late answer for another country is dropped. */
    data class StatesLoaded(val countryCode: String, val result: Result<List<AddressItem>>) : AddressIntent
}
