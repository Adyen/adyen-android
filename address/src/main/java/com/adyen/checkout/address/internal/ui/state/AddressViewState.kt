/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import androidx.compose.runtime.Immutable
import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ViewState

/**
 * @param elements The address form, in the order the shopper sees it. Each element carries everything needed to render
 * it.
 * @param countryPickerViewState The country picker screen state, separate from the country [AddressFormElement].
 * @param statePickerViewState The state picker screen state, or null while the selected country has no loaded states.
 */
@Immutable
internal data class AddressViewState(
    val elements: List<AddressFormElement>,
    val countryPickerViewState: PickerViewState,
    val statePickerViewState: PickerViewState?,
) : ViewState

/**
 * What a picker screen shows.
 *
 * @param title The label of the field that opens the picker.
 * @param regions The countries or states the shopper can pick, in the order they are shown.
 * @param selected The current choice, or null when there is none.
 */
@Immutable
internal data class PickerViewState(
    val title: CheckoutLocalizationKey,
    val regions: List<AddressRegion>,
    val selected: AddressRegion?,
)
