/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.DualBrandedCardHandler
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.RequirementPolicy
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class CardViewStateProducer(
    private val dualBrandedCardHandler: DualBrandedCardHandler,
) : ViewStateProducer<CardComponentState, CardViewState> {

    override fun produce(state: CardComponentState): CardViewState {
        val supportedDetectedCardTypes = state.detectedCardTypes.filter { it.isSupported }
        val firstSupportedDetectedCardType = supportedDetectedCardTypes.firstOrNull()

        val dualBrandData = dualBrandedCardHandler.processDetectedCardTypes(
            detectedCardTypes = state.detectedCardTypes,
            selectedBrand = state.selectedCardBrand,
        )
        val detectedCardBrands = getDetectedCardBrands(dualBrandData, firstSupportedDetectedCardType?.cardBrand)

        return CardViewState(
            cardNumber = state.cardNumber.toViewState(
                trailingIcon = getCardNumberTrailingIcon(state.cardNumber),
            ),
            expiryDate = state.expiryDate.takeIf { it.requirementPolicy !is RequirementPolicy.Hidden }?.toViewState(
                trailingIcon = getExpiryDateTrailingIcon(state.expiryDate),
            ),
            securityCode = state.securityCode.takeIf { it.requirementPolicy !is RequirementPolicy.Hidden }?.toViewState(
                trailingIcon = getSecurityCodeTrailingIcon(state.securityCode, detectedCardBrands),
            ),
            holderName = state.takeIf {
                it.holderName.requirementPolicy !is RequirementPolicy.Hidden
            }?.holderName?.toViewState(),
            storePaymentMethod = state.storePaymentMethod,
            isStorePaymentFieldVisible = state.isStorePaymentFieldVisible,
            supportedCardBrands = state.supportedCardBrands,
            isSupportedCardBrandsShown = supportedDetectedCardTypes.isEmpty(),
            detectedCardBrands = detectedCardBrands,
            isLoading = state.isLoading,
            dualBrandData = dualBrandData,
        )
    }

    private fun getDetectedCardBrands(dualBrandData: DualBrandData?, fallbackDetectedCardBrand: CardBrand?) = when {
        dualBrandData != null -> listOf(dualBrandData.brandOptionFirst.brand, dualBrandData.brandOptionSecond.brand)
        fallbackDetectedCardBrand != null -> listOf(fallbackDetectedCardBrand)
        else -> listOf()
    }

    private fun getCardNumberTrailingIcon(cardNumber: TextInputComponentState): CardNumberTrailingIcon {
        val isInvalid = cardNumber.errorMessage != null && cardNumber.showError
        return when {
            isInvalid -> CardNumberTrailingIcon.Warning
            else -> CardNumberTrailingIcon.BrandLogos
        }
    }

    private fun getExpiryDateTrailingIcon(
        expiryDate: TextInputComponentState,
    ): ExpiryDateTrailingIcon {
        val isValid = expiryDate.errorMessage == null && expiryDate.text.isNotEmpty()
        val isInvalid = expiryDate.errorMessage != null && expiryDate.showError

        return when {
            isValid -> ExpiryDateTrailingIcon.Checkmark
            isInvalid -> ExpiryDateTrailingIcon.Warning
            else -> ExpiryDateTrailingIcon.Placeholder
        }
    }

    private fun getSecurityCodeTrailingIcon(
        securityCode: TextInputComponentState,
        detectedCardBrands: List<CardBrand>
    ): SecurityCodeTrailingIcon {
        val isValid = securityCode.errorMessage == null && securityCode.text.isNotEmpty()
        val isInvalid = securityCode.errorMessage != null && securityCode.showError
        val isAmex = detectedCardBrands.firstOrNull()?.let { detectedCard ->
            detectedCard.txVariant == CardType.AMERICAN_EXPRESS.txVariant
        }

        return when {
            isValid -> SecurityCodeTrailingIcon.Checkmark
            isInvalid -> SecurityCodeTrailingIcon.Warning
            isAmex == true -> SecurityCodeTrailingIcon.PlaceholderAmex
            else -> SecurityCodeTrailingIcon.PlaceholderDefault
        }
    }
}
