/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldState

internal data class MBWayState(
    val countryCodeFieldState: ComponentFieldState<String> = ComponentFieldState(value = ""),
    val localPhoneNumberFieldState: ComponentFieldState<String> = ComponentFieldState(value = ""),
)
