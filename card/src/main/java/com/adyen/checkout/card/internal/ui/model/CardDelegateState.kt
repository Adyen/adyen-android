/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 10/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateState
import com.adyen.checkout.core.ui.model.ExpiryDate

internal data class CardDelegateState(
    val cardNumberDelegateState: ComponentFieldDelegateState<String>,
    val expiryDateDelegateState: ComponentFieldDelegateState<ExpiryDate>,
    val securityCodeDelegateState: ComponentFieldDelegateState<String>,
    val holderNameDelegateState: ComponentFieldDelegateState<String>,
    val socialSecurityNumberDelegateState: ComponentFieldDelegateState<String>,
    // TODO: Should these be separated?
    val kcpBirthDateOrTaxNumberDelegateState: ComponentFieldDelegateState<String>,
    val kcpCardPasswordDelegateState: ComponentFieldDelegateState<String>,
    val installmentDelegateState: ComponentFieldDelegateState<InstallmentModel?>,
) : DelegateState {
    override val isValid: Boolean = true
}
