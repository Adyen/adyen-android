/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 10/7/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ViewState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputViewState

internal data class MBWayViewState(
    val countries: List<CountryModel>,
    val selectedCountryCode: CountryModel,
    val phoneNumber: TextInputViewState,
    val isLoading: Boolean,
) : ViewState
