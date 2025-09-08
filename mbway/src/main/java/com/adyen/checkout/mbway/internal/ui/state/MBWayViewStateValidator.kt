/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 1/9/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.core.components.internal.ui.state.ViewStateValidator
import com.adyen.checkout.mbway.R

internal class MBWayViewStateValidator : ViewStateValidator<MBWayViewState> {

    override fun validate(viewState: MBWayViewState): MBWayViewState {
        val telephoneNumber = viewState.countryCode.callingCode + viewState.phoneNumber.text
        val phoneNumberError = if (!ValidationUtils.isPhoneNumberValid(telephoneNumber)) {
            R.string.checkout_mbway_phone_number_not_valid
        } else {
            null
        }
        return viewState.copy(
            phoneNumber = viewState.phoneNumber.copy(errorMessage = phoneNumberError),
        )
    }

    override fun isValid(viewState: MBWayViewState): Boolean {
        return viewState.phoneNumber.errorMessage == null
    }

    override fun highlightAllValidationErrors(viewState: MBWayViewState): MBWayViewState {
        val hasPhoneNumberError = viewState.phoneNumber.errorMessage != null

        return viewState.copy(
            phoneNumber = viewState.phoneNumber.copy(
                showError = hasPhoneNumberError,
                isFocused = hasPhoneNumberError,
            ),
        )
    }
}
