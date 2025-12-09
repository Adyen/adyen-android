/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.core.components.internal.ui.model.CountryModel
import com.adyen.checkout.core.components.internal.ui.state.ComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputState

internal data class MBWayComponentState(
    val countries: List<CountryModel>,
    val countryCode: CountryModel,
    val phoneNumber: TextInputState,
    val isLoading: Boolean,
) : ComponentState
