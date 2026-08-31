/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.PayButtonViewState

/**
 * @param elements The MB Way form, in the order the shopper sees it. Each element carries everything needed to render
 * it.
 * @param isLoading Whether the component is waiting on a payment, which stops the shopper interacting with the form.
 * @param payButtonViewState The pay button, or null when the merchant provides their own.
 * @param countryPickerViewState The country picker screen. One property per secondary screen, named rather than found
 * among [elements], because a secondary screen is a separate screen with its own data.
 */
@Immutable
internal data class MBWayViewState(
    val elements: List<MBWayFormElement>,
    val isLoading: Boolean,
    val payButtonViewState: PayButtonViewState?,
    val countryPickerViewState: CountryPickerViewState,
) : ViewState

/**
 * What the country picker screen shows. Never null: MB Way always supports at least one country, so the screen always
 * has something to offer.
 *
 * @param countries The countries the shopper can choose from.
 * @param selectedCountry The current choice.
 */
@Immutable
internal data class CountryPickerViewState(
    val countries: List<CountryModel>,
    val selectedCountry: CountryModel,
)
