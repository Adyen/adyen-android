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
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateState
import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardDelegateState(
    val cardNumberDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val selectedCardIndexDelegateState: ComponentFieldDelegateState<Int> =
        ComponentFieldDelegateState(value = -1),
    val cardSecurityCodeDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val cardExpiryDateDelegateState: ComponentFieldDelegateState<ExpiryDate> =
        ComponentFieldDelegateState(value = EMPTY_DATE),
    val cardHolderNameDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
//    val socialSecurityNumberDelegateState: ComponentFieldDelegateState<String>,
    // TODO: Should these be separated?
//    val kcpBirthDateOrTaxNumberDelegateState: ComponentFieldDelegateState<String>,
//    val kcpCardPasswordDelegateState: ComponentFieldDelegateState<String>,
//    val installmentDelegateState: ComponentFieldDelegateState<InstallmentModel?>,
    val detectedCardTypes: List<DetectedCardType> = emptyList(),
    val selectedOrFirstCardType: DetectedCardType? = null,
    val cvcUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
    val enableLuhnCheck: Boolean? = null,
    val isBrandSupported: Boolean? = null,
    val isHolderNameRequired: Boolean = false,
) : DelegateState {
    override val isValid: Boolean = true
}
