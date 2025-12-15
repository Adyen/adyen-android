/*
 * Copyright (c) 2025 Adyen N.V.
 *
 * This file is open source and available under the MIT license. See the LICENSE file for more info.
 *
 * Created by ozgur on 15/12/2025.
 */

package com.adyen.checkout.card.internal.ui.state

import com.adyen.checkout.card.internal.ui.DualBrandedCardHandler
import com.adyen.checkout.card.internal.ui.model.DualBrandData
import com.adyen.checkout.core.common.CardBrand
import com.adyen.checkout.core.components.internal.ui.state.ViewStateProducer

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
            cardNumber = state.cardNumber,
            expiryDate = state.expiryDate,
            securityCode = state.securityCode,
            holderName = state.holderName,
            isHolderNameRequired = state.isHolderNameRequired,
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
}
