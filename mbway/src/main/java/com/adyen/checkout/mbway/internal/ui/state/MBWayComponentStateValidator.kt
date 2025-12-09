/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 9/12/2025.
 */

package com.adyen.checkout.mbway.internal.ui.state

import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator

internal class MBWayComponentStateValidator : ComponentStateValidator<MBWayComponentState> {

    override fun validate(state: MBWayComponentState): MBWayComponentState {
        // TODO - Should we strip the leading zeros? Depends on validation
        val telephoneNumber = state.countryCode.callingCode + state.phoneNumber.text
        val phoneNumberError = if (!ValidationUtils.isPhoneNumberValid(telephoneNumber)) {
            CheckoutLocalizationKey.MBWAY_INVALID_PHONE_NUMBER
        } else {
            null
        }
        return state.copy(
            phoneNumber = state.phoneNumber.copy(errorMessage = phoneNumberError),
        )
    }

    override fun isValid(state: MBWayComponentState): Boolean {
        return state.phoneNumber.errorMessage == null
    }
}
