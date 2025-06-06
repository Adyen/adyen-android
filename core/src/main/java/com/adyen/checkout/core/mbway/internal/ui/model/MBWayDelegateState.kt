/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.core.mbway.internal.ui.model

import com.adyen.checkout.core.internal.ui.state.DelegateState
import com.adyen.checkout.core.internal.ui.state.model.DelegateFieldState

internal data class MBWayDelegateState(
    val countries: List<String>,
    val countryCodeFieldState: DelegateFieldState<String> = DelegateFieldState(value = ""),
    val localPhoneNumberFieldState: DelegateFieldState<String> = DelegateFieldState(value = ""),
) : DelegateState {

    override val isValid: Boolean
        get() = countryCodeFieldState.validation?.isValid() == true &&
            localPhoneNumberFieldState.validation?.isValid() == true
}
