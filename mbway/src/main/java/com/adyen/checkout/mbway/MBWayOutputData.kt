/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 18/12/2020.
 */
package com.adyen.checkout.mbway

import android.text.TextUtils
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.util.ValidationUtils
import com.adyen.checkout.components.validation.ValidatedField

class MBWayOutputData(mobilePhoneNumber: String) : OutputData {

    companion object {
        private fun validateMobileNumber(mobileNumber: String): ValidatedField<String> {
            return if (!TextUtils.isEmpty(mobileNumber) && ValidationUtils.isPhoneNumberValid(mobileNumber)) {
                ValidatedField(mobileNumber, ValidatedField.Validation.VALID)
            } else {
                ValidatedField(mobileNumber, ValidatedField.Validation.INVALID)
            }
        }
    }

    val mobilePhoneNumberField: ValidatedField<String> = validateMobileNumber(mobilePhoneNumber)
    override fun isValid(): Boolean {
        return mobilePhoneNumberField.isValid
    }
}
