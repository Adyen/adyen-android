/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateState
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal data class MBWayDelegateState(
    val countries: List<CountryModel>,
    val countryCodeFieldState: ComponentFieldDelegateState<CountryModel>,
    val localPhoneNumberFieldState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
) : DelegateState {
    override val isValid: Boolean
        get() = countryCodeFieldState.validation?.isValid() == true &&
                localPhoneNumberFieldState.validation?.isValid() == true
}
