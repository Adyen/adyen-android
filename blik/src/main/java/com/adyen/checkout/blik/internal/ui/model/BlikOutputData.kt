/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by josephj on 4/12/2020.
 */
package com.adyen.checkout.blik.internal.ui.model

import com.adyen.checkout.blik.R
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.components.core.internal.ui.model.Validation
import com.adyen.checkout.core.old.AdyenLogLevel
import com.adyen.checkout.core.old.internal.util.adyenLog

internal class BlikOutputData(blikCode: String) : OutputData {

    val blikCodeField: FieldState<String>
    override val isValid: Boolean
        get() = blikCodeField.validation.isValid()

    private fun getBlikCodeValidation(blikCode: String): Validation {
        try {
            if (blikCode.isNotEmpty()) blikCode.toInt()
        } catch (e: NumberFormatException) {
            adyenLog(AdyenLogLevel.ERROR, e) { "Failed to parse blik code to Integer" }
            return Validation.Invalid(R.string.checkout_blik_code_not_valid)
        }
        return if (blikCode.length == BLIK_CODE_LENGTH) {
            Validation.Valid
        } else {
            Validation.Invalid(R.string.checkout_blik_code_not_valid)
        }
    }

    companion object {
        private const val BLIK_CODE_LENGTH = 6
    }

    init {
        blikCodeField = FieldState(blikCode, getBlikCodeValidation(blikCode))
    }
}
