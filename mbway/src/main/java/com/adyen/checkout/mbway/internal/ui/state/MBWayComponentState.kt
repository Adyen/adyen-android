/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.ComponentFieldState

internal data class MBWayComponentState(
    val countries: List<CountryModel>,
    val countryCodeFieldState: ComponentFieldState<CountryModel>,
    val localPhoneNumberFieldState: ComponentFieldState<String> = ComponentFieldState(value = ""),
) : ComponentState {

    override val isValid: Boolean
        get() = countryCodeFieldState.validation?.isValid() == true &&
            localPhoneNumberFieldState.validation?.isValid() == true
}
