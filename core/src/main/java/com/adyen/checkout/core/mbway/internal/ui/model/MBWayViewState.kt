/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.model

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.internal.ui.state.model.ViewFieldState
import com.adyen.checkout.core.internal.ui.state.model.toViewFieldState

internal data class MBWayViewState(
    val countries: List<CountryModel>,
    val countryCodeFieldState: ViewFieldState<CountryModel>,
    val phoneNumberFieldState: ViewFieldState<String>,
)

internal fun MBWayDelegateState.toViewState() = MBWayViewState(
    countries = this.countries,
    countryCodeFieldState = this.countryCodeFieldState.toViewFieldState(),
    phoneNumberFieldState = this.localPhoneNumberFieldState.toViewFieldState(),
)
