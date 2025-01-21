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
import com.adyen.checkout.ui.core.internal.ui.model.CountryModel

internal data class MBWayDelegateState(
    val countries: List<CountryModel>,
    val initiallySelectedCountry: CountryModel?,
    val countryCodeFieldState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val localPhoneNumberFieldState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
) {
    val isValid: Boolean
        get() = countryCodeFieldState.validation?.isValid() == true &&
                localPhoneNumberFieldState.validation?.isValid() == true
}

internal fun MBWayDelegateState.toViewState() = MBWayViewState(
    countries = this.countries,
    initiallySelectedCountry = initiallySelectedCountry,
    phoneNumberFieldState = this.localPhoneNumberFieldState.toComponentFieldViewState(),
)

// TODO: Create component state from MBWayDelegateState

internal fun <T> ComponentFieldDelegateState<T>.toComponentFieldViewState() =
    ComponentFieldViewState(
        value = value,
        errorMessageId = takeIf { fieldState ->
            fieldState.shouldShowValidationError()
        }?.validation.let { it as? Validation.Invalid }?.reason,
    )

internal fun <T> ComponentFieldDelegateState<T>.shouldShowValidationError() =
    !this.hasFocus || this.isValidationErrorCheckForced
