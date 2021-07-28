/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card

import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.FieldState

data class CardOutputData(
    val cardNumberState: FieldState<String>,
    val expiryDateState: FieldState<ExpiryDate>,
    val securityCodeState: FieldState<String>,
    val holderNameState: FieldState<String>,
    val socialSecurityNumberState: FieldState<String>,
    val isStoredPaymentMethodEnable: Boolean,
    val cvcUIState: CvcUIState,
    val detectedCardTypes: List<DetectedCardType>,
    val socialSecurityNumberVisibility: SocialSecurityNumberVisibility?
) : OutputData {
    override fun isValid(): Boolean {
        return cardNumberState.validation.isValid() &&
            expiryDateState.validation.isValid() &&
            securityCodeState.validation.isValid() &&
            holderNameState.validation.isValid() &&
            socialSecurityNumberState.validation.isValid()
    }
}
