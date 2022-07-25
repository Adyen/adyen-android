/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/12/2020.
 */
package com.adyen.checkout.mbway

import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.components.util.ValidationUtils

class MBWayOutputData(mobilePhoneNumber: String) : OutputData {

    val mobilePhoneNumberFieldState: FieldState<String> = validateMobileNumber(mobilePhoneNumber)

    override val isValid: Boolean
        get() = mobilePhoneNumberFieldState.validation.isValid()

    private fun validateMobileNumber(mobileNumber: String): FieldState<String> {
        return if (mobileNumber.isNotEmpty() && ValidationUtils.isPhoneNumberValid(mobileNumber)) {
            FieldState(
                mobileNumber,
                Validation.Valid
            )
        } else {
            FieldState(
                mobileNumber,
                Validation.Invalid(R.string.checkout_mbway_phone_number_not_valid)
            )
        }
    }
}
