/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 15/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.Validation

internal data class MBWayDelegateState(
    val countryCodeFieldState: ComponentFieldDelegateState<String> = ComponentFieldDelegateState(
        value = "",
        validation = Validation.Valid,
    ),
    val localPhoneNumberFieldState: ComponentFieldDelegateState<String> = ComponentFieldDelegateState(value = ""),
    // TODO: Find another way to optimise this and not trigger state change multiple times when field value changes
    val showValidationErrorsRegardlessFocus:Boolean = false,
) {
    val isValid: Boolean
        get() =
            countryCodeFieldState.validation?.isValid() == true &&
                localPhoneNumberFieldState.validation?.isValid() == true
}

internal fun MBWayDelegateState.toViewState() = MBWayViewState(
    phoneNumberFieldState = ComponentFieldViewState(
        value = this.localPhoneNumberFieldState.value,
        errorMessageId = this.localPhoneNumberFieldState.takeIf { fieldState ->
            !fieldState.hasFocus || this.showValidationErrorsRegardlessFocus
        }?.validation.let { it as? Validation.Invalid }?.reason,
    ),
)
