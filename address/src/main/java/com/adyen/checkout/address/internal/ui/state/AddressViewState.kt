/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 29/9/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ViewState

/**
 * @param elements The address form, in the order the shopper sees it. Each element carries everything needed to
 * render it.
 * @param countryPickerViewState The country picker screen state, separate from the country [AddressFormElement].
 */
@Immutable
internal data class AddressViewState(
    val elements: List<AddressFormElement>,
    val countryPickerViewState: CountryPickerViewState,
) : ViewState

/**
 * What the country picker screen shows.
 *
 * @param countries The countries the shopper can choose from, in the order they are shown.
 * @param selectedCountry The current choice, or null when nothing is picked yet.
 */
@Immutable
internal data class CountryPickerViewState(
    val countries: List<CountryModel>,
    val selectedCountry: CountryModel?,
)
