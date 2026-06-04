/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.helper.isHiddenCardType
import com.adyen.checkout.card.internal.ui.model.CardNumberTrailingIcon
import com.adyen.checkout.card.internal.ui.model.ExpiryDateTrailingIcon
import com.adyen.checkout.card.internal.ui.model.PostalCodeTrailingIcon
import com.adyen.checkout.card.internal.ui.model.SecurityCodeTrailingIcon
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.common.CardType
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer
import com.adyen.checkout.core.components.internal.ui.state.model.TextInputComponentState
import com.adyen.checkout.core.components.internal.ui.state.model.toViewState

internal class CardViewStateProducer : ViewStateProducer<CardComponentState, CardViewState> {

    override fun produce(state: CardComponentState): CardViewState {
        // we only show all supported card brands when the setting is enabled
        // and we do not detect any brands for this specific card
        val isSupportedCardBrandsShown = state.showSupportedCardBrandLogos && when (state.cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> true

            is CardBrandState.SingleUnreliableBrand,
            is CardBrandState.SingleReliableBrand,
            is CardBrandState.SingleReliableWithHiddenBrand,
            is CardBrandState.DualBrand,
            is CardBrandState.DualBrandWithShopperSelection -> false
        }

        val detectedCardBrands = getDetectedCardBrands(state.cardBrandState)
        val isCardScanButtonVisible = state.isCardScanningAvailable && state.cardNumber.text.isEmpty()

        return CardViewState(
            cardNumber = state.cardNumber.toViewState(
                trailingIcon = getCardNumberTrailingIcon(state.cardNumber, isCardScanButtonVisible),
            ),
            expiryDate = state.expiryDate.toViewState(
                trailingIcon = getExpiryDateTrailingIcon(state.expiryDate),
            ),
            securityCode = state.securityCode.toViewState(
                trailingIcon = getSecurityCodeTrailingIcon(state.securityCode, detectedCardBrands),
            ),
            holderName = state.holderName.toViewState(),
            socialSecurityNumber = state.socialSecurityNumber.toViewState(),
            kcpBirthDateOrTaxNumber = state.kcpBirthDateOrTaxNumber.toViewState(),
            kcpCardPassword = state.kcpCardPassword.toViewState(),
            postalCode = state.postalCode.toViewState(
                trailingIcon = getPostalCodeTrailingIcon(state.postalCode)
            ),
            storePaymentMethod = state.storePaymentMethod,
            isStorePaymentFieldVisible = state.isStorePaymentFieldVisible,
            supportedCardBrands = state.supportedCardBrands.filterNot {
                isHiddenCardType(it.txVariant)
            },
            isSupportedCardBrandsShown = isSupportedCardBrandsShown,
            detectedCardBrands = detectedCardBrands,
            isLoading = state.isLoading,
            isCardScanButtonVisible = isCardScanButtonVisible,
        )
    }

    // detected card brands are shown on the UI in all flows
    private fun getDetectedCardBrands(cardBrandState: CardBrandState): List<CardBrand> {
        return when (cardBrandState) {
            is CardBrandState.NoBrandsDetected,
            is CardBrandState.UnsupportedBrand,
            is CardBrandState.HiddenBrand -> emptyList()

            is CardBrandState.SingleUnreliableBrand -> listOf(cardBrandState.cardBrandData.cardBrand)
            is CardBrandState.SingleReliableBrand -> listOf(cardBrandState.cardBrandData.cardBrand)
            is CardBrandState.SingleReliableWithHiddenBrand -> listOf(cardBrandState.cardBrandData.cardBrand)
            is CardBrandState.DualBrand -> cardBrandState.cardBrandDataList.map { it.cardBrand }
            is CardBrandState.DualBrandWithShopperSelection -> cardBrandState.cardBrandDataList.map { it.cardBrand }
        }
    }

    private fun getCardNumberTrailingIcon(
        cardNumber: TextInputComponentState,
        isCardScanButtonVisible: Boolean,
    ): CardNumberTrailingIcon {
        val isInvalid = cardNumber.errorMessage != null && cardNumber.showError
        return when {
            isInvalid -> CardNumberTrailingIcon.Warning
            isCardScanButtonVisible -> CardNumberTrailingIcon.ScanButton
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

    private fun getPostalCodeTrailingIcon(
        postalCode: TextInputComponentState
    ): PostalCodeTrailingIcon {
        val isInvalid = postalCode.errorMessage != null && postalCode.showError

        return when {
            isInvalid -> PostalCodeTrailingIcon.Warning
            else -> PostalCodeTrailingIcon.Placeholder
        }
    }
}
