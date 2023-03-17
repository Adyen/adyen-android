/*
 * Copyright (c) 2023 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by oscars on 15/3/2023.
 */

package com.adyen.checkout.ui.core.internal.ui

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.components.core.internal.util.ValidationUtils
import com.adyen.checkout.ui.core.R
import com.adyen.checkout.ui.core.internal.ui.model.PhoneNumberInputData
import com.adyen.checkout.ui.core.internal.ui.model.PhoneNumberOutputData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
interface PhoneNumberDelegate {
    val phoneNumberOutputData: PhoneNumberOutputData
    val phoneNumberOutputDataFlow: Flow<PhoneNumberOutputData>
    var onInputDataChangedListener: (() -> Unit)?
    fun updatePhoneNumberInputData(update: PhoneNumberInputData.() -> Unit)
}

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
class DefaultPhoneNumberDelegate : PhoneNumberDelegate {

    private val inputData: PhoneNumberInputData = PhoneNumberInputData()

    override val phoneNumberOutputData: PhoneNumberOutputData get() = outputDataFlow.value

    private val outputDataFlow = MutableStateFlow(createOutputData())
    override val phoneNumberOutputDataFlow: Flow<PhoneNumberOutputData> = outputDataFlow

    override var onInputDataChangedListener: (() -> Unit)? = null

    override fun updatePhoneNumberInputData(update: PhoneNumberInputData.() -> Unit) {
        inputData.update()
        onInputDataChanged()
        onInputDataChangedListener?.invoke()
    }

    private fun onInputDataChanged() {
        val outputData = createOutputData()
        outputDataFlow.tryEmit(outputData)
    }

    private fun createOutputData() = PhoneNumberOutputData(
        phoneNumber = validatePhoneNumber(inputData.countryCode, inputData.everythingAfterCountryCode)
    )

    private fun validatePhoneNumber(countryCode: String, phoneNumber: String): FieldState<String> {
        val sanitizedNumber = phoneNumber.trimStart('0')
        val fullPhoneNumber = countryCode + sanitizedNumber
        val validation = if (fullPhoneNumber.isNotEmpty() && ValidationUtils.isPhoneNumberValid(fullPhoneNumber)) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_phone_number_invalid)
        }
        return FieldState(fullPhoneNumber, validation)
    }
}
