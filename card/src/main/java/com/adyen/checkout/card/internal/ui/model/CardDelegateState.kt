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
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.AddressInputModel
import com.adyen.checkout.components.core.internal.ui.model.ComponentFieldDelegateState
import com.adyen.checkout.components.core.internal.ui.model.state.DelegateState
import com.adyen.checkout.core.internal.ui.model.EMPTY_DATE
import com.adyen.checkout.core.ui.model.ExpiryDate
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.model.AddressListItem
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData

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
    val socialSecurityNumberDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val kcpBirthDateOrTaxNumberDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val kcpCardPasswordDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val addressPostalCodeDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val storedPaymentMethodSwitchDelegateState: ComponentFieldDelegateState<Boolean> =
        ComponentFieldDelegateState(value = false),
    val installmentOptionDelegateState: ComponentFieldDelegateState<InstallmentModel?> =
        ComponentFieldDelegateState(value = null),
    val addressLookupDelegateState: ComponentFieldDelegateState<String> =
        ComponentFieldDelegateState(value = ""),
    val componentParams: CardComponentParams,
    val detectedCardTypes: List<DetectedCardType> = emptyList(),
    val selectedOrFirstCardType: DetectedCardType? = null,
    val cardBrands: List<CardListItem> = emptyList(),
    val publicKey: String? = null,
    val isCardBrandListVisible: Boolean = false,
    val isDualBranded: Boolean = false,
    val cvcUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
    val expiryDateUIState: InputFieldUIState = InputFieldUIState.REQUIRED,
    val holderNameUIState: InputFieldUIState = if (componentParams.isHolderNameRequired) {
        InputFieldUIState.REQUIRED
    } else {
        InputFieldUIState.HIDDEN
    },
    val enableLuhnCheck: Boolean = true,
    val isBrandSupported: Boolean? = null,
    val updatedCountryOptions: List<AddressListItem> = emptyList(),
    val updatedStateOptions: List<AddressListItem> = emptyList(),
    val addressFormUIState: AddressFormUIState = AddressFormUIState.fromAddressParams(
        componentParams.addressParams,
    ),
    val showStorePaymentField: Boolean = componentParams.isStorePaymentFieldVisible,
    val installmentOptions: List<InstallmentModel>,
    val address: AddressInputModel = AddressInputModel(),
    val addressState: AddressOutputData,
    val isAddressOptional: Boolean = false,
) : DelegateState {

    override val isValid: Boolean
        get() =
            cardNumberDelegateState.validation?.isValid() == true &&
                cardExpiryDateDelegateState.validation?.isValid() == true &&
                cardSecurityCodeDelegateState.validation?.isValid() == true &&
                cardHolderNameDelegateState.validation?.isValid() == true &&
                socialSecurityNumberDelegateState.validation?.isValid() == true &&
                kcpBirthDateOrTaxNumberDelegateState.validation?.isValid() == true &&
                kcpCardPasswordDelegateState.validation?.isValid() == true &&
                installmentOptionDelegateState.validation?.isValid() == true &&
                addressState.isValid
}
