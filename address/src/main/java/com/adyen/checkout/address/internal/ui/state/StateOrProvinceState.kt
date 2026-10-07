/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 7/10/2026.
 */

package com.adyen.checkout.address.internal.ui.state

import com.adyen.checkout.address.internal.ui.model.AddressRegion
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

/**
 * The state or province element. What it shows depends on the answer of the states dataset of the selected country.
 */
internal sealed interface StateOrProvinceState {

    /**
     * The states of the selected country are being fetched. Nothing is shown.
     *
     * @param pendingCode A prefilled state, kept until the states arrive so it can be selected then.
     */
    data class Loading(val pendingCode: String?) : StateOrProvinceState

    /**
     * The country has no states (its dataset is empty or does not exist), or no country is selected. Nothing is shown.
     */
    data object Unavailable : StateOrProvinceState

    /** The country has states. A picker is shown, and the picked state is stored as its code. */
    data class Options(
        val regions: List<AddressRegion>,
        val picker: PickerInputComponentState<String>,
    ) : StateOrProvinceState

    /** The states could not be loaded. A text field is shown, so the shopper is never blocked. */
    data class FreeText(val input: TextInputComponentState) : StateOrProvinceState
}
