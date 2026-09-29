/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the address form, carrying everything needed to render it.
 */
internal sealed interface AddressFormElement {

    val id: AddressFormElementId

    /**
     * The row that shows the chosen country and opens the country picker. The countries themselves belong to that
     * screen, not to this row, so they live in [CountryPickerViewState].
     *
     * @param errorMessage The error to show, or null while there is none or it is held back.
     */
    data class Country(
        val selectedCountry: CountryModel?,
        val errorMessage: CheckoutLocalizationKey?,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.COUNTRY
    }

    data class PostalCode(
        val textInputViewState: TextInputViewState,
    ) : AddressFormElement {
        override val id get() = AddressFormElementId.POSTAL_CODE
    }
}
