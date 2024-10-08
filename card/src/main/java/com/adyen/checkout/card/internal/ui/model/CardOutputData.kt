/*
 * Copyright (c) 2020 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by caiof on 27/8/2020.
 */
package com.adyen.checkout.card.internal.ui.model

import androidx.annotation.RestrictTo
import androidx.annotation.StringRes
import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.card.internal.ui.view.InstallmentModel
import com.adyen.checkout.components.core.internal.ui.model.FieldState
import com.adyen.checkout.components.core.internal.ui.model.OutputData
import com.adyen.checkout.ui.core.internal.ui.AddressFormUIState
import com.adyen.checkout.ui.core.internal.ui.model.AddressOutputData
import com.adyen.checkout.ui.core.internal.ui.model.ExpiryDate

@RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
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
    val shouldStorePaymentMethod: Boolean,
    val cvcUIState: InputFieldUIState,
    val expiryDateUIState: InputFieldUIState,
    val holderNameUIState: InputFieldUIState,
    val showStorePaymentField: Boolean,
    val detectedCardTypes: List<DetectedCardType>,
    val isSocialSecurityNumberRequired: Boolean,
    val isKCPAuthRequired: Boolean,
    val addressUIState: AddressFormUIState,
    val installmentOptions: List<InstallmentModel>,
    val cardBrands: List<CardListItem>,
    val isDualBranded: Boolean,
    @StringRes
    val kcpBirthDateOrTaxNumberHint: Int?,
    val isCardListVisible: Boolean,
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
