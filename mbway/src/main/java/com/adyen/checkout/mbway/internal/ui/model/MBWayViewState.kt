/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.Validation

internal data class MBWayViewState(
    val phoneNumberFieldState: ComponentFieldViewState<String>,
)

internal fun MBWayState.toViewState() = MBWayViewState(
    phoneNumberFieldState = ComponentFieldViewState(
        value = this.localPhoneNumberFieldState.value,
        errorMessageId = this.localPhoneNumberFieldState.takeUnless {
            it.hasFocus
        }?.validation.let { it as? Validation.Invalid }?.reason,
    ),
)
