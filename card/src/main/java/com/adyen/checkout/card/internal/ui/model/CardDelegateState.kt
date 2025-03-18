/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.CardValidationMapper
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardDelegateState(
    val cardNumberDelegateState: ComponentFieldDelegateState<String> = ComponentFieldDelegateState(
        value = "",
    ),
//    val expiryDateDelegateState: ComponentFieldDelegateState<ExpiryDate>,
//    val securityCodeDelegateState: ComponentFieldDelegateState<String>,
//    val holderNameDelegateState: ComponentFieldDelegateState<String>,
//    val socialSecurityNumberDelegateState: ComponentFieldDelegateState<String>,
    // TODO: Should these be separated?
//    val kcpBirthDateOrTaxNumberDelegateState: ComponentFieldDelegateState<String>,
//    val kcpCardPasswordDelegateState: ComponentFieldDelegateState<String>,
//    val installmentDelegateState: ComponentFieldDelegateState<InstallmentModel?>,
    // TODO: We should make sure that this is updated correctly and perhaps also when card number changes?
    val detectedCardTypes: List<DetectedCardType> = emptyList(),
    val enableLuhnCheck: Boolean? = null,
    val isBrandSupported: Boolean? = null,
) : DelegateState {
    override val isValid: Boolean = true
}
