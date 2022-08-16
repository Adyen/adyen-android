/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card

import androidx.annotation.StringRes
import com.adyen.checkout.card.data.CardType
import com.adyen.checkout.card.data.DetectedCardType
import com.adyen.checkout.card.data.ExpiryDate
import com.adyen.checkout.card.ui.model.AddressListItem
import com.adyen.checkout.components.base.OutputData
import com.adyen.checkout.components.ui.ComponentMode
import com.adyen.checkout.components.ui.FieldState

data class CardOutputData(
    val cardNumberState: FieldState<String>,
    val expiryDateState: FieldState<ExpiryDate>,
    val securityCodeState: FieldState<String>,
    val holderNameState: FieldState<String>,
    val socialSecurityNumberState: FieldState<String>,
    val kcpBirthDateOrTaxNumberState: FieldState<String>,
    val kcpCardPasswordState: FieldState<String>,
    val addressState: AddressOutputData,
    val installmentState: FieldState<InstallmentModel?>,
    val isStoredPaymentMethodEnable: Boolean,
    val cvcUIState: InputFieldUIState,
    val expiryDateUIState: InputFieldUIState,
    val holderNameUIState: InputFieldUIState,
    val showStorePaymentField: Boolean,
    val detectedCardTypes: List<DetectedCardType>,
    val isSocialSecurityNumberRequired: Boolean,
    val isKCPAuthRequired: Boolean,
    val addressUIState: AddressFormUIState,
    val installmentOptions: List<InstallmentModel>,
    val countryOptions: List<AddressListItem>,
    val stateOptions: List<AddressListItem>,
    val supportedCardTypes: List<CardType>,
    val isDualBranded: Boolean,
    @StringRes
    val kcpBirthDateOrTaxNumberHint: Int?,
    val componentMode: ComponentMode,
) : OutputData {

    override val isValid: Boolean
        get() =
            cardNumberState.validation.isValid() &&
                expiryDateState.validation.isValid() &&
                securityCodeState.validation.isValid() &&
                holderNameState.validation.isValid() &&
                socialSecurityNumberState.validation.isValid() &&
                kcpBirthDateOrTaxNumberState.validation.isValid() &&
                kcpCardPasswordState.validation.isValid() &&
                installmentState.validation.isValid() &&
                addressState.isValid
}
