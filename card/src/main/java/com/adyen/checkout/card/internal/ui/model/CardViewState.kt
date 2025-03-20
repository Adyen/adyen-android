/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ararat on 18/3/2025.
 */

package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldViewState
import com.adyen.checkout.components.core.internal.ui.model.toComponentFieldViewState
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
data class CardViewState(
    val cardNumberFieldState: ComponentFieldViewState<String>,
    val cardSecurityCodeFieldState: ComponentFieldViewState<String>,
    val cardExpiryDateFieldState: ComponentFieldViewState<ExpiryDate>,
    val cardHolderNameFieldState: ComponentFieldViewState<String>,
    val socialSecurityNumberFieldState: ComponentFieldViewState<String>,
    val kcpBirthDateOrTaxNumberFieldState: ComponentFieldViewState<String>,
    val kcpCardPasswordFieldState: ComponentFieldViewState<String>,
    val addressPostalCodeFieldState: ComponentFieldViewState<String>,
    val storedPaymentMethodSwitchFieldState: ComponentFieldViewState<Boolean>,
    val addressUIState: AddressFormUIState,
    val cvcUIState: InputFieldUIState,
    val expiryDateUIState: InputFieldUIState,
    val holderNameUIState: InputFieldUIState,
    val showStorePaymentField: Boolean,
)

internal fun CardDelegateState.toViewState() = CardViewState(
    cardNumberFieldState = this.cardNumberDelegateState.toComponentFieldViewState(),
    cardSecurityCodeFieldState = this.cardSecurityCodeDelegateState.toComponentFieldViewState(),
    cardExpiryDateFieldState = this.cardExpiryDateDelegateState.toComponentFieldViewState(),
    cardHolderNameFieldState = this.cardHolderNameDelegateState.toComponentFieldViewState(),
    socialSecurityNumberFieldState = this.socialSecurityNumberDelegateState.toComponentFieldViewState(),
    kcpBirthDateOrTaxNumberFieldState = this.kcpBirthDateOrTaxNumberDelegateState.toComponentFieldViewState(),
    kcpCardPasswordFieldState = this.kcpCardPasswordDelegateState.toComponentFieldViewState(),
    addressPostalCodeFieldState = this.addressPostalCodeDelegateState.toComponentFieldViewState(),
    storedPaymentMethodSwitchFieldState = this.storedPaymentMethodSwitchDelegateState.toComponentFieldViewState(),
    addressUIState = this.addressFormUIState,
    cvcUIState = this.cvcUIState,
    expiryDateUIState = this.expiryDateUIState,
    holderNameUIState = this.holderNameUIState,
    showStorePaymentField = this.showStorePaymentField,
)
