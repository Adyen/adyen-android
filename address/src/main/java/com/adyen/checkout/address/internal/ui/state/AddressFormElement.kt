/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the address form, carrying everything needed to render it, including the label of the selected
 * country's layout. A row that is not shown has no element.
 */
internal sealed interface AddressFormElement {

    /** The composition key, and what tells the state layer which field a focus event came from. */
    val id: AddressFormElementId

    /**
     * The row that shows the selected country and opens the country picker.
     *
     * @param selected The selected country, named from the loaded countries or, while they are not loaded, by the
     * platform. Null when no country is selected.
     * @param errorMessage The error to show, only once it is visible.
     */
    data class Country(
        val label: CheckoutLocalizationKey,
        val selected: AddressRegion?,
        val errorMessage: CheckoutLocalizationKey?,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.COUNTRY
    }

    data class Street(
        val label: CheckoutLocalizationKey,
        val textInputViewState: TextInputViewState,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.STREET
    }

    data class HouseNumberOrName(
        val label: CheckoutLocalizationKey,
        val textInputViewState: TextInputViewState,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.HOUSE_NUMBER_OR_NAME
    }

    data class PostalCode(
        val label: CheckoutLocalizationKey,
        val maxLength: Int,
        val textInputViewState: TextInputViewState,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.POSTAL_CODE
    }

    data class City(
        val label: CheckoutLocalizationKey,
        val textInputViewState: TextInputViewState,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.CITY
    }

    /**
     * The row that shows the picked state and opens the state picker. It only exists for a country with states.
     *
     * @param selected The selected state, named from the loaded states or, while they are not loaded, by its code. Null
     * when no state is selected.
     * @param errorMessage The error to show, only once it is visible.
     */
    data class StateOrProvince(
        val label: CheckoutLocalizationKey,
        val selected: AddressRegion?,
        val errorMessage: CheckoutLocalizationKey?,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.STATE_OR_PROVINCE
    }
}
