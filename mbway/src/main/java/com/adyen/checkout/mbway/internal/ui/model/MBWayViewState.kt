/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.toComponentFieldViewState
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal data class MBWayViewState(
    val countries: List<CountryModel>,
    val countryCodeFieldState: ComponentFieldViewState<CountryModel>,
    val phoneNumberFieldState: ComponentFieldViewState<String>,
)

// TODO: Write a test for this
internal fun MBWayDelegateState.toViewState() = MBWayViewState(
    countries = this.countries,
    countryCodeFieldState = this.countryCodeFieldState.toComponentFieldViewState(),
    phoneNumberFieldState = this.localPhoneNumberFieldState.toComponentFieldViewState(),
)
