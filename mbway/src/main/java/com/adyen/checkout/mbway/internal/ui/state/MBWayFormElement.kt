/*
 * Copyright (c) 2026 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 31/8/2026.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

/**
 * One row of the MB Way form, carrying everything needed to render it.
 */
internal sealed interface MBWayFormElement {

    val id: MBWayFormElementId

    /**
     * The row that shows the chosen country and opens the country picker. The countries themselves belong to that
     * screen, not to this row, so they live in [CountryPickerViewState].
     */
    data class CountryCode(
        val selectedCountry: CountryModel,
    ) : MBWayFormElement {
        override val id get() = MBWayFormElementId.COUNTRY_CODE
    }

    data class PhoneNumber(
        val textInputViewState: TextInputViewState,
        val callingCode: String,
    ) : MBWayFormElement {
        override val id get() = MBWayFormElementId.PHONE_NUMBER
    }
}
