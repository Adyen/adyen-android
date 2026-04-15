/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.data.model.DetectedCardType
import com.adyen.checkout.core.common.localization.CheckoutLocalizationKey
import com.adyen.checkout.core.components.internal.ui.state.ComponentStateValidator
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState

internal class CardComponentStateValidator(
    private val cardValidationMapper: CardValidationMapper,
) : ComponentStateValidator<CardComponentState> {

    override fun validate(state: CardComponentState): CardComponentState {
        val isReliable = state.detectedCardTypes.any { it.isReliable }
        val supportedDetectedCardTypes = state.detectedCardTypes.filter { it.isSupported }
        val firstSupportedDetectedCardType = supportedDetectedCardTypes.firstOrNull()

        val cardNumberError = validateCardNumber(state.cardNumber, firstSupportedDetectedCardType, isReliable)
        val expiryDateError = validateExpiryDate(state.expiryDate, firstSupportedDetectedCardType)
        val securityCodeError =
            validateSecurityCode(
                state.securityCode,
                firstSupportedDetectedCardType,
            )
        val holderNameError = validateHolderName(state.holderName)
        val socialSecurityNumberError = validateSocialSecurityNumber(state.socialSecurityNumber)
        val kcpBirthDateOrTaxNumberError = validateKcpBirthDateOrTaxNumber(state.kcpBirthDateOrTaxNumber)
        val kcpCardPasswordError = validateKcpCardPassword(state.kcpCardPassword)

        return state.copy(
            cardNumber = state.cardNumber.copy(errorMessage = cardNumberError),
            expiryDate = state.expiryDate.copy(errorMessage = expiryDateError),
            securityCode = state.securityCode.copy(errorMessage = securityCodeError),
            holderName = state.holderName.copy(errorMessage = holderNameError),
            socialSecurityNumber = state.socialSecurityNumber.copy(errorMessage = socialSecurityNumberError),
            kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.copy(errorMessage = kcpBirthDateOrTaxNumberError),
            kcpCardPassword = state.kcpCardPassword.copy(errorMessage = kcpCardPasswordError),
        )
    }

    override fun isValid(state: CardComponentState): Boolean {
        return state.cardNumber.errorMessage == null &&
            state.expiryDate.errorMessage == null &&
            state.securityCode.errorMessage == null &&
            state.holderName.errorMessage == null &&
            state.socialSecurityNumber.errorMessage == null &&
            state.kcpBirthDateOrTaxNumber.errorMessage == null &&
            state.kcpCardPassword.errorMessage == null
    }

    private fun validateCardNumber(
        cardNumber: TextInputComponentState,
        selectedOrFirstCardType: DetectedCardType?,
        isReliable: Boolean,
    ): CheckoutLocalizationKey? {
        val enableLuhnCheck = selectedOrFirstCardType?.enableLuhnCheck ?: true
        val shouldFailWithUnsupportedBrand = selectedOrFirstCardType == null && isReliable

        return cardValidationMapper.mapCardNumberValidation(
            validation = CardValidationUtils.validateCardNumber(
                cardNumber = cardNumber,
                enableLuhnCheck = enableLuhnCheck,
                isBrandSupported = !shouldFailWithUnsupportedBrand,
            ),
        )
    }

    private fun validateExpiryDate(
        expiryDate: TextInputComponentState,
        selectedOrFirstCardType: DetectedCardType?,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapExpiryDateValidation(
            validation = CardValidationUtils.validateExpiryDate(
                expiryDate = expiryDate,
                fieldPolicy = selectedOrFirstCardType?.expiryDatePolicy,
            ),
        )
    }

    private fun validateSecurityCode(
        securityCode: TextInputComponentState,
        selectedOrFirstCardType: DetectedCardType?,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapSecurityCodeValidation(
            validation = CardValidationUtils.validateSecurityCode(
                securityCode = securityCode,
                detectedCardType = selectedOrFirstCardType,
            ),
        )
    }

    private fun validateHolderName(
        holderName: TextInputComponentState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapHolderNameValidation(
            validation = CardValidationUtils.validateHolderName(holderName = holderName),
        )
    }

    private fun validateSocialSecurityNumber(
        socialSecurityNumber: TextInputComponentState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapSocialSecurityNumberValidation(
            validation = CardValidationUtils.validateSocialSecurityNumber(socialSecurityNumber = socialSecurityNumber),
        )
    }

    private fun validateKcpBirthDateOrTaxNumber(
        kcpBirthDateOrTaxNumber: TextInputComponentState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapKCPBirthDateOrTaxNumberValidation(
            validation = CardValidationUtils.validateKCPBirthDateOrTaxNumber(
                kcpBirthDateOrTaxNumber = kcpBirthDateOrTaxNumber,
            ),
        )
    }

    private fun validateKcpCardPassword(
        kcpCardPassword: TextInputComponentState,
    ): CheckoutLocalizationKey? {
        return cardValidationMapper.mapKCPCardPasswordValidation(
            validation = CardValidationUtils.validateKCPCardPassword(kcpCardPassword = kcpCardPassword),
        )
    }
}
