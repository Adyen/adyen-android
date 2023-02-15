/*
 * Copyright (c) 2022 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by atef on 20/9/2022.
 */

package com.adyen.checkout.onlinebankingcore.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState
import com.adyen.checkout.components.ui.Validation
import com.adyen.checkout.onlinebankingcore.R

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class OnlineBankingOutputData(
    val selectedIssuer: OnlineBankingModel? = null
) : OutputData {

    val selectedIssuerField: FieldState<OnlineBankingModel?> = FieldState(
        value = selectedIssuer,
        validation = if (selectedIssuer == null) {
            Validation.Invalid(R.string.checkout_online_banking_hint)
        } else {
            Validation.Valid
        }
    )

    override val isValid
        get() = selectedIssuerField.validation.isValid()
}
