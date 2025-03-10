/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 14/1/2025.
 */

package com.adyen.checkout.mbway.internal.ui.model

import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.Validation
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

// TODO: Write a test for this
internal fun <T> ComponentFieldDelegateState<T>.toComponentFieldViewState() =
    ComponentFieldViewState(
        value = value,
        hasFocus = hasFocus,
        errorMessageId = takeIf { fieldState ->
            fieldState.shouldShowValidationError()
        }?.validation.let { it as? Validation.Invalid }?.reason,
    )

// Validation error should be shown, when the field loses its focus or when we manually trigger a validation
internal fun <T> ComponentFieldDelegateState<T>.shouldShowValidationError() =
    !this.hasFocus || this.shouldHighlightValidationError
